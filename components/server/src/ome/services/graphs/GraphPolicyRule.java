/*
 * Copyright (C) 2014-2015 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.services.graphs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ome.model.IObject;
import ome.services.graphs.GraphPolicy.Details;

import org.apache.commons.lang.mutable.MutableBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * A graph policy rule specifies a component of a {@link GraphPolicy}.
 * It is designed to be conveniently created using Spring by supplying configuration metadata to the bean container.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.1.0
 */
public class GraphPolicyRule {
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphPolicyRule.class);

    private static final Pattern NEW_TERM_PATTERN =
            Pattern.compile("(\\w+\\:)?(\\!?[\\w]+)?(\\[\\!?[EDIO]+\\])?(\\{\\!?[iroa]+\\})?(\\/\\!?[udon]+)?(\\;\\S+)?");
    private static final Pattern PREDICATE_PATTERN = Pattern.compile("\\;(\\w+)\\=([^\\;]+)(\\;\\S+)?");
    private static final Pattern EXISTING_TERM_PATTERN = Pattern.compile("(\\w+)");
    private static final Pattern CHANGE_PATTERN = Pattern.compile("(\\w+\\:)(\\[[EDIO\\-]\\])?(\\{[iroa]\\})?(\\/n)?");

    private List<String> matches = Collections.emptyList();
    private List<String> changes = Collections.emptyList();
    private String errorMessage = null;

    /**
     * @param matches the match conditions for this policy rule, comma-separated
     */
    public void setMatches(String matches) {
        this.matches = ImmutableList.copyOf(matches.split(",\\s*"));
    }

    /**
     * @param change the changes caused by this policy rule, comma-separated
     */
    public void setChanges(String changes) {
        this.changes = ImmutableList.copyOf(changes.split(",\\s*"));
    }

    /**
     * @param message the error message triggered by this policy rule
     */
    public void setError(String message) {
        errorMessage = message;
    }

    @Override
    public String toString() {
        final String trigger = Joiner.on(", ").join(matches);
        final String consequence;

        if (errorMessage == null) {
            consequence = "to " + Joiner.on(", ").join(changes);
        } else {
            consequence = "is error: " + errorMessage;
        }

        return trigger + ' ' + consequence;
    }

    /**
     * Matches model object instances term on either side of a link among objects.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.1.0
     */
    private static interface TermMatch {
        /**
         * If this matches the given term.
         * Does not adjust {@code namedTerms} or {@code isCheckAllPermissions} unless the match succeeds,
         * in which case sets {@code isCheckAllPermissions} to {@code false} if
         * {@code details.isCheckPermissions == false}.
         * @param predicates the predicate matchers that may be named in policy rules
         * @param namedTerms the name dictionary of matched terms (updated by this method)
         * @param isCheckAllPermissions if permissions are to be checked for all of the matched objects (updated by this method)
         * @param details the details of the term
         * @return if the term matches
         * @throws GraphException if the match attempt could not be completed
         */
        boolean isMatch(Map<String, GraphPolicyRulePredicate> predicates, Map<String, Details> namedTerms,
                MutableBoolean isCheckAllPermissions, Details details) throws GraphException;
    }

    /**
     * {@inheritDoc}
     * Matches an existing named term.
     */
    private static class ExistingTermMatch implements TermMatch {
        final String termName;

        /**
         * Construct an existing term match.
         * @param termName the name of the existing term
         */
        ExistingTermMatch(String termName) {
            this.termName = termName;
        }

        @Override
        public boolean isMatch(Map<String, GraphPolicyRulePredicate> predicates, Map<String, Details> namedTerms,
                MutableBoolean isCheckAllPermissions, Details details) {
            return details.equals(namedTerms.get(termName));
        }
    }

    /**
     * {@inheritDoc}
     * May define a new named term.
     */
    private static class NewTermMatch implements TermMatch {
        private static Set<GraphPolicy.Action> ONLY_EXCLUDE = Collections.singleton(GraphPolicy.Action.EXCLUDE);
        private static Set<GraphPolicy.Action> ALL_ACTIONS = EnumSet.allOf(GraphPolicy.Action.class);
        private static Set<GraphPolicy.Orphan> ALL_ORPHANS = EnumSet.allOf(GraphPolicy.Orphan.class);

        private final String termName;
        private final Class<? extends IObject> requiredClass;
        private final Class<? extends IObject> prohibitedClass;
        private final Collection<GraphPolicy.Action> permittedActions;
        private final Collection<GraphPolicy.Orphan> permittedOrphans;
        private final Set<GraphPolicy.Ability> requiredAbilities;
        private final Set<GraphPolicy.Ability> prohibitedAbilities;
        private final Boolean isCheckPermissions;
        private final Map<String, String> predicateArguments;

        /**
         * Construct a new term match. All arguments may be {@code null}.
         * @param termName the name of the term, so as to allow references to it
         * @param requiredClass a class of which the object may be an instance
         * @param prohibitedClass a class of which the object may not be an instance
         * @param permittedActions the actions permitted for the object (assumed to be only {@link GraphPolicy.Action#EXCLUDE}
         * if {@code permittedOrphans} is non-{@code null})
         * @param permittedOrphans the orphan statuses permitted for the object
         * @param requiredAbilities the abilities that the user must have to operate upon the object
         * @param prohibitedAbilities the abilities that the user must not have to operate upon the object
         * @param isCheckPermissions if permissions are being checked for the object, may be {@code null}
         * @param predicateArguments arguments that must satisfy named predicates, may be {@code null}
         */
        NewTermMatch(String termName, Class<? extends IObject> requiredClass, Class<? extends IObject> prohibitedClass,
                Collection<GraphPolicy.Action> permittedActions, Collection<GraphPolicy.Orphan> permittedOrphans,
                Collection<GraphPolicy.Ability> requiredAbilities, Collection<GraphPolicy.Ability> prohibitedAbilities,
                Boolean isCheckPermissions, Map<String, String> predicateArguments) {
            this.termName = termName;
            this.requiredClass = requiredClass;
            this.prohibitedClass = prohibitedClass;
            if (permittedOrphans == null) {
                if (permittedActions == null) {
                    this.permittedActions = ALL_ACTIONS;
                } else {
                    this.permittedActions = ImmutableSet.copyOf(permittedActions);
                }
                this.permittedOrphans = ALL_ORPHANS;
            } else {
                this.permittedActions = ONLY_EXCLUDE;
                this.permittedOrphans = ImmutableSet.copyOf(permittedOrphans);
            }
            if (requiredAbilities == null) {
                this.requiredAbilities = ImmutableSet.of();
            } else {
                this.requiredAbilities = ImmutableSet.copyOf(requiredAbilities);
            }
            if (prohibitedAbilities == null) {
                this.prohibitedAbilities = ImmutableSet.of();
            } else {
                this.prohibitedAbilities = ImmutableSet.copyOf(prohibitedAbilities);
            }
            this.isCheckPermissions = isCheckPermissions;
            this.predicateArguments = predicateArguments;
        }

        @Override
        public boolean isMatch(Map<String, GraphPolicyRulePredicate> predicates, Map<String, Details> namedTerms,
                MutableBoolean isCheckAllPermissions, Details details) throws GraphException {
            final Class<? extends IObject> subjectClass = details.subject.getClass();
            final boolean previousIsCheckAllPermissions = isCheckAllPermissions.booleanValue();
            if (previousIsCheckAllPermissions && !details.isCheckPermissions) {
                /* note that this match causes a permissions override to be applied to changes */
                isCheckAllPermissions.setValue(false);
            }
            if ((requiredClass == null || requiredClass.isAssignableFrom(subjectClass)) &&
                (prohibitedClass == null || !prohibitedClass.isAssignableFrom(subjectClass)) &&
                permittedActions.contains(details.action) &&
                (details.action != GraphPolicy.Action.EXCLUDE || permittedOrphans.contains(details.orphan)) &&
                Sets.difference(requiredAbilities, details.permissions).isEmpty() &&
                Sets.intersection(prohibitedAbilities, details.permissions).isEmpty() &&
                (isCheckPermissions == null || isCheckPermissions == details.isCheckPermissions)) {
                if (predicateArguments != null) {
                    for (final Entry<String, String> predicateArgument : predicateArguments.entrySet()) {
                        final String predicateName = predicateArgument.getKey();
                        final String predicateValue = predicateArgument.getValue();
                        final GraphPolicyRulePredicate predicate = predicates.get(predicateName);
                        if (predicate == null) {
                            throw new GraphException("unknown predicate: " + predicateName);
                        }
                        if (!predicate.isMatch(details, predicateValue)) {
                            return false;
                        }
                    }
                }
                if (termName == null) {
                    return true;
                } else {
                    /* check the named term against the dictionary of such terms */
                    final Details oldDetails = namedTerms.get(termName);
                    if (oldDetails == null) {
                        namedTerms.put(termName, details);
                        return true;
                    } else if (oldDetails.equals(details)) {
                        return true;
                    }
                }
            }
            isCheckAllPermissions.setValue(previousIsCheckAllPermissions);
            return false;
        }
    }

    /**
     * Matches relationships between a pair of linked model object instance terms.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.1.0
     */
    private static class RelationshipMatch {
        private final TermMatch leftTerm;
        private final TermMatch rightTerm;
        private final String propertyName;
        private final Boolean notNullable;
        private final Boolean sameOwner;

        /**
         * Construct a new relationship match.
         * @param leftTerm the match for the left term (the object doing the linking)
         * @param rightTerm the match for the right term (the linked object)
         * @param propertyName the name of the property of the left term that has the right term as its value
         * @param notNullable if the property is not nullable (or {@code null} if either is permitted)
         * @param sameOwner if the two terms must have the same owner
         * ({@code null} if it doesn't matter, {@code false} if they must differ)
         */
        RelationshipMatch(TermMatch leftTerm, TermMatch rightTerm, String propertyName, Boolean notNullable, Boolean sameOwner) {
            this.leftTerm = leftTerm;
            this.rightTerm = rightTerm;
            this.propertyName = propertyName == null ? null : '.' + propertyName;
            this.notNullable = notNullable;
            this.sameOwner = sameOwner;
        }

        /**
         * If this matches the given relationship.
         * Does not adjust {@code namedTerms} or {@code isCheckAllPermissions} unless the match succeeds,
         * in which case sets {@code isCheckAllPermissions} to {@code false} if
         * {@code leftDetails.isCheckPermissions && rightDetails.isCheckPermissions == false}.
         * @param predicates the predicate matchers that may be named in policy rules
         * @param namedTerms the name dictionary of matched terms (to be updated by this method)
         * @param isCheckAllPermissions if permissions are to be checked for all of the matched objects
         * @param leftDetails the details of the left term, holding the property
         * @param rightDetails the details of the right term, being a value of the property
         * @param classProperty the name of the declaring class and property
         * @param notNullable if the property is not nullable
         * @return if the relationship matches
         * @throws GraphException if the match attempt could not be completed
         */
        boolean isMatch(Map<String, GraphPolicyRulePredicate> predicates, Map<String, Details> namedTerms,
                MutableBoolean isCheckAllPermissions,
                Details leftDetails, Details rightDetails, String classProperty, boolean notNullable) throws GraphException {
            if ((this.sameOwner != null && leftDetails.ownerId != null && rightDetails.ownerId != null &&
                 this.sameOwner != leftDetails.ownerId.equals(rightDetails.ownerId)) ||
                (this.notNullable != null && this.notNullable != notNullable) ||
                (this.propertyName != null && !classProperty.endsWith(propertyName))) {
                return false;
            }
            final Map<String, Details> newNamedTerms = new HashMap<String, Details>(namedTerms);
            final MutableBoolean newIsCheckAllPermissions = new MutableBoolean(isCheckAllPermissions.booleanValue());
            final boolean isMatch = leftTerm.isMatch(predicates, newNamedTerms, newIsCheckAllPermissions, leftDetails) &&
                                   rightTerm.isMatch(predicates, newNamedTerms, newIsCheckAllPermissions, rightDetails);
            if (isMatch) {
                namedTerms.putAll(newNamedTerms);
                isCheckAllPermissions.setValue(newIsCheckAllPermissions.booleanValue());
            }
            return isMatch;
        }

        /**
         * @return the name of the existing left term required for this relationship match,
         * or {@code null} if the left term is not an existing term
         */
        String getExistingLeftTerm() {
            return leftTerm instanceof ExistingTermMatch ? ((ExistingTermMatch) leftTerm).termName : null;
        }

        /**
         * @return the name of the existing right term required for this relationship match,
         * or {@code null} if the right term is not an existing term
         */
        String getExistingRightTerm() {
            return rightTerm instanceof ExistingTermMatch ? ((ExistingTermMatch) rightTerm).termName : null;
        }
}

    /**
     * Matches conditions available via {@link GraphPolicy#isCondition(String)}.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.1.0
     */
    private static class ConditionMatch {
        final boolean set;
        final String name;

        /**
         * Construct a new condition match.
         * @param set if the condition should be set
         * @param name the name of the condition
         */
        ConditionMatch(boolean set, String name) {
            this.set = set;
            this.name = name;
        }
    }

    /**
     * A change to effect if a rule's matchers match.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.1.0
     */
    private static class Change {
        private final String namedTerm;
        private final GraphPolicy.Action action;
        private final GraphPolicy.Orphan orphan;
        private final boolean isOverridePermissions;

        /**
         * Construct a change instance.
         * @param namedTerm the term to affect
         * @param action the effect to have on the action, {@code null} for no effect
         * @param orphan the effect to have on the orphan status, {@code null} for no effect
         * @param isOverridePermissions if permissions checking should be overridden
         */
        Change(String namedTerm, GraphPolicy.Action action, GraphPolicy.Orphan orphan, boolean isOverridePermissions) {
            this.namedTerm = namedTerm;
            this.action = action;
            this.orphan = orphan;
            this.isOverridePermissions = isOverridePermissions;
        }

        /**
         * Effect the change.
         * @param namedTerms the name dictionary of matched terms
         * @return the details of the changed term
         * @throws GraphException if the named term is not defined in the matching
         */
        Details toChanged(Map<String, Details> namedTerms) throws GraphException {
            final Details details = namedTerms.get(namedTerm);
            if (details == null) {
                throw new GraphException("policy rule: reference to unknown term " + namedTerm);
            }
            if (action != null) {
                details.action = action;
            }
            if (orphan != null) {
                details.orphan = orphan;
            }
            if (isOverridePermissions) {
                details.isCheckPermissions = false;
            }
            return details;
        }

        /**
         * @return if this change actually affects the term's action or orphan status
         */
        boolean isEffectiveChange() {
            return action != null || orphan != null;
        }
    }

    /**
     * A policy rule with matchers and changes that can now be applied having been parsed from the text-based configuration.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.1.0
     */
    private static class ParsedPolicyRule {
        final String asString;
        final List<TermMatch> termMatchers;
        final List<RelationshipMatch> relationshipMatchers;
        final List<ConditionMatch> conditionMatchers;
        final List<Change> changes;
        final String errorMessage;

        /**
         * Construct a policy rule.
         * @param asString a String representation of this rule,
         * recognizably corresponding to its original text-based configuration.
         * @param termMatchers the term matchers that must apply if the changes are to be applied
         * @param relationshipMatchers the relationship matchers that must apply if the changes are to be applied
         * @param conditionMatchers the condition matchers that must apply if the changes are to be applied
         * @param changes the effects of this rule, guarded by the matchers
         */
        ParsedPolicyRule(String asString, List<TermMatch> termMatchers, List<RelationshipMatch> relationshipMatchers,
                List<ConditionMatch> conditionMatchers, List<Change> changes) {
            this.asString = asString;
            this.termMatchers = termMatchers;
            this.relationshipMatchers = relationshipMatchers;
            this.conditionMatchers = conditionMatchers;
            this.changes = changes;
            this.errorMessage = null;
        }

        /**
         * Construct a policy rule.
         * @param asString a String representation of this rule,
         * recognizably corresponding to its original text-based configuration.
         * @param termMatchers the term matchers that must apply if the changes are to be applied
         * @param relationshipMatchers the relationship matchers that must apply if the changes are to be applied
         * @param conditionMatchers the condition matchers that must apply if the changes are to be applied
         * @param changes the effects of this rule, guarded by the matchers
         */
        ParsedPolicyRule(String asString, List<TermMatch> termMatchers, List<RelationshipMatch> relationshipMatchers,
                List<ConditionMatch> conditionMatchers, String errorMessage) {
            this.asString = asString;
            this.termMatchers = termMatchers;
            this.relationshipMatchers = relationshipMatchers;
            this.conditionMatchers = conditionMatchers;
            this.changes = Collections.emptyList();
            this.errorMessage = errorMessage;
        }
    }

    /**
     * Parse a term match from a textual representation.
     * @param graphPathBean the graph path bean
     * @param term some text
     * @return the term match parsed from the text
     * @throws GraphException if the parse failed
     */
    private static TermMatch parseTermMatch(GraphPathBean graphPathBean, String term) throws GraphException {
        /* determine if new or existing term */

        final Matcher existingTermMatcher = EXISTING_TERM_PATTERN.matcher(term);

        if (existingTermMatcher.matches()) {
            return new ExistingTermMatch(existingTermMatcher.group(1));
        }

        final Matcher newTermMatcher = NEW_TERM_PATTERN.matcher(term);
        if (!newTermMatcher.matches()) {
            throw new GraphException("failed to parse match term " + term);
        }

        /* note parse results */

        final String termName;
        final Class<? extends IObject> requiredClass;
        final Class<? extends IObject> prohibitedClass;
        final Collection<GraphPolicy.Action> permittedActions;
        final Collection<GraphPolicy.Orphan> permittedOrphans;
        final Collection<GraphPolicy.Ability> requiredAbilities;
        final Collection<GraphPolicy.Ability> prohibitedAbilities;
        Boolean isCheckPermissions = null;
        final Map<String, String> predicateArguments;

        /* parse term name, if any */

        final String termNameGroup = newTermMatcher.group(1);
        if (termNameGroup == null) {
            termName = null;
        } else {
            termName = termNameGroup.substring(0, termNameGroup.length() - 1);
        }

        /* parse class name, if any */

        final String classNameGroup = newTermMatcher.group(2);
        if (classNameGroup == null) {
            requiredClass = null;
            prohibitedClass = null;
        } else if (classNameGroup.charAt(0) == '!') {
            requiredClass = null;
            prohibitedClass = graphPathBean.getClassForSimpleName(classNameGroup.substring(1));
            if (prohibitedClass == null) {
                throw new GraphException("unknown class named in " + term);
            }
        } else {
            requiredClass = graphPathBean.getClassForSimpleName(classNameGroup);
            prohibitedClass = null;
            if (requiredClass == null) {
                throw new GraphException("unknown class named in " + term);
            }
        }

        /* parse actions, if any */

        final String actionGroup = newTermMatcher.group(3);
        if (actionGroup == null) {
            permittedActions = null;
        } else {
            final EnumSet<GraphPolicy.Action> actions = EnumSet.noneOf(GraphPolicy.Action.class);
            boolean invert = false;
            for (final char action : actionGroup.toCharArray()) {
                if (action == 'E') {
                    actions.add(GraphPolicy.Action.EXCLUDE);
                } else if (action == 'D') {
                    actions.add(GraphPolicy.Action.DELETE);
                } else if (action == 'I') {
                    actions.add(GraphPolicy.Action.INCLUDE);
                } else if (action == 'O') {
                    actions.add(GraphPolicy.Action.OUTSIDE);
                } else if (action == '!') {
                    invert = true;
                }
            }
            permittedActions = invert ? EnumSet.complementOf(actions) : actions;
        }

        /* parse orphans, if any */

        final String orphanGroup = newTermMatcher.group(4);
        if (orphanGroup == null) {
            permittedOrphans = null;
        } else {
            final EnumSet<GraphPolicy.Orphan> orphans = EnumSet.noneOf(GraphPolicy.Orphan.class);
            boolean invert = false;
            for (final char orphan : orphanGroup.toCharArray()) {
                if (orphan == 'i') {
                    orphans.add(GraphPolicy.Orphan.IRRELEVANT);
                } else if (orphan == 'r') {
                    orphans.add(GraphPolicy.Orphan.RELEVANT);
                } else if (orphan == 'o') {
                    orphans.add(GraphPolicy.Orphan.IS_LAST);
                } else if (orphan == 'a') {
                    orphans.add(GraphPolicy.Orphan.IS_NOT_LAST);
                } else if (orphan == '!') {
                    invert = true;
                }
            }
            permittedOrphans = invert ? EnumSet.complementOf(orphans) : orphans;
        }

        /* parse abilities, if any; also permissions checking */

        final String abilityGroup = newTermMatcher.group(5);
        if (abilityGroup == null) {
            requiredAbilities = null;
            prohibitedAbilities = null;
        } else {
            final EnumSet<GraphPolicy.Ability> abilities = EnumSet.noneOf(GraphPolicy.Ability.class);
            boolean required = true;
            for (final char ability : abilityGroup.toCharArray()) {
                if (ability == 'u') {
                    abilities.add(GraphPolicy.Ability.UPDATE);
                } else if (ability == 'd') {
                    abilities.add(GraphPolicy.Ability.DELETE);
                } else if (ability == 'o') {
                    abilities.add(GraphPolicy.Ability.OWN);
                } else if (ability == 'n') {
                    isCheckPermissions = !required;
                } else if (ability == '!') {
                    required = false;
                }
            }
            if (required) {
                requiredAbilities = abilities;
                prohibitedAbilities = null;
            } else {
                requiredAbilities = null;
                prohibitedAbilities = abilities;
            }
        }

        /* parse named predicate arguments, if any */

        if (newTermMatcher.group(6) == null) {
            predicateArguments = null;
        } else {
            predicateArguments = new HashMap<String, String>();
            String remainingPredicates = newTermMatcher.group(6);
            while (remainingPredicates != null) {
                final Matcher predicateMatcher = PREDICATE_PATTERN.matcher(remainingPredicates);
                if (!predicateMatcher.matches()) {
                    throw new GraphException("failed to parse predicates suffixing match term " + term);
                }
                predicateArguments.put(predicateMatcher.group(1), predicateMatcher.group(2));
                remainingPredicates = predicateMatcher.group(3);
            }
        }

        /* construct new term match */

        return new NewTermMatch(termName, requiredClass, prohibitedClass, permittedActions, permittedOrphans,
                requiredAbilities, prohibitedAbilities, isCheckPermissions, predicateArguments);

    }

    /**
     * Parse a relationship match from a textual representation.
     * @param graphPathBean the graph path bean
     * @param leftTerm the first <q>word</q> of text
     * @param equals the second <q>word</q> of text
     * @param rightTerm the third <q>word</q> of text
     * @return the relationship match parsed from the text
     * @throws GraphException if the parse failed
     */
    private static RelationshipMatch parseRelationshipMatch(GraphPathBean graphPathBean,
            String leftTerm, String equals, String rightTerm)
            throws GraphException {
        final Boolean sameOwner;
        final int slash = equals.indexOf('/');
        if (slash < 0) {
            sameOwner = null;
        } else {
            sameOwner = equals.endsWith("/o");
            equals = equals.substring(0, slash);
        }
        final Boolean notNullable;
        if ("=".equals(equals)) {
            notNullable = null;
        } else if ("==".equals(equals)) {
            notNullable = Boolean.TRUE;
        } else if ("=?".equals(equals)) {
            notNullable = Boolean.FALSE;
        } else {
            throw new GraphException(Joiner.on(' ').join("failed to parse match", leftTerm, equals, rightTerm));
        }
        if (rightTerm.indexOf('.') > 0) {
            final String forSwap = rightTerm;
            rightTerm = leftTerm;
            leftTerm = forSwap;
        }
        final String propertyName;
        final int periodIndex = leftTerm.indexOf('.');
        if (periodIndex > 0) {
            propertyName = leftTerm.substring(periodIndex + 1);
            leftTerm = leftTerm.substring(0, periodIndex);
        } else {
            propertyName = null;
        }
        final TermMatch leftTermMatch = parseTermMatch(graphPathBean, leftTerm);
        final TermMatch rightTermMatch = parseTermMatch(graphPathBean, rightTerm);
        return new RelationshipMatch(leftTermMatch, rightTermMatch, propertyName, notNullable, sameOwner);
    }

    /**
     * Parse a change from a textual representation.
     * @param change some text
     * @return the change parsed from the text
     * @throws GraphException if the parse failed
     */
    private static Change parseChange(String change) throws GraphException {
        final Matcher matcher = CHANGE_PATTERN.matcher(change);
        if (!matcher.matches()) {
            throw new GraphException("failed to parse change " + change);
        }

        final String termName;
        final GraphPolicy.Action action;
        final GraphPolicy.Orphan orphan;
        final boolean isOverridePermissions;

        /* parse term name */

        final String termNameGroup = matcher.group(1);
        termName = termNameGroup.substring(0, termNameGroup.length() - 1);

        /* parse actions, if any */

        if (matcher.group(2) == null) {
            action = null;
        } else {
            switch (matcher.group(2).charAt(1)) {
            case 'E':
                action = GraphPolicy.Action.EXCLUDE;
                break;
            case 'D':
                action = GraphPolicy.Action.DELETE;
                break;
            case 'I':
                action = GraphPolicy.Action.INCLUDE;
                break;
            case 'O':
                action = GraphPolicy.Action.OUTSIDE;
                break;
            default:
                action = null;
                break;
            }
        }

        /* parse orphans, if any */

        if (matcher.group(3) == null) {
            orphan = null;
        } else {
            switch (matcher.group(3).charAt(1)) {
            case 'i':
                orphan = GraphPolicy.Orphan.IRRELEVANT;
                break;
            case 'r':
                orphan = GraphPolicy.Orphan.RELEVANT;
                break;
            case 'o':
                orphan = GraphPolicy.Orphan.IS_LAST;
                break;
            case 'a':
                orphan = GraphPolicy.Orphan.IS_NOT_LAST;
                break;
            default:
                orphan = null;
                break;
            }
        }

        /* parse permissions override, if any */

        if (matcher.group(4) == null) {
            isOverridePermissions = false;
        } else {
            switch (matcher.group(4).charAt(1)) {
            case 'n':
                isOverridePermissions = true;
                break;
            default:
                isOverridePermissions = false;
                break;
            }
        }

        return new Change(termName, action, orphan, isOverridePermissions);
    }

    /**
     * Convert the text-based rules as specified in the configuration metadata into a policy applicable in
     * model object graph traversal.
     * (A more advanced effort could construct an efficient decision tree, but that optimization may be premature.)
     * @param graphPathBean the graph path bean
     * @param rules the rules to apply
     * @return a policy for graph traversal by {@link GraphTraversal}
     * @throws GraphException if the text-based rules could not be parsed
     */
    public static GraphPolicy parseRules(GraphPathBean graphPathBean,
            Collection<GraphPolicyRule> rules) throws GraphException {
        final List<ParsedPolicyRule> policyRules = new ArrayList<ParsedPolicyRule>();
        for (final GraphPolicyRule policyRule : rules) {
            final List<TermMatch> termMatches = new ArrayList<TermMatch>();
            final List<RelationshipMatch> relationshipMatches = new ArrayList<RelationshipMatch>();
            final List<ConditionMatch> conditionMatches = new ArrayList<ConditionMatch>();
            for (final String match : policyRule.matches) {
                final String[] words = match.trim().split("\\s+");
                if (words.length == 1) {
                    final String word = words[0];
                    if (word.startsWith("$")) {
                        conditionMatches.add(new ConditionMatch(true, word.substring(1)));
                    } else if (word.startsWith("!$")) {
                        conditionMatches.add(new ConditionMatch(false, word.substring(2)));
                    } else {
                        termMatches.add(parseTermMatch(graphPathBean, word));
                    }
                } else if (words.length == 3) {
                    relationshipMatches.add(parseRelationshipMatch(graphPathBean, words[0], words[1], words[2]));
                } else {
                    throw new GraphException("failed to parse match " + match);
                }
            }
            if (policyRule.errorMessage == null) {
                final List<Change> changes = new ArrayList<Change>();
                for (final String change : policyRule.changes) {
                    changes.add(parseChange(change.trim()));
                }
                policyRules.add(new ParsedPolicyRule(policyRule.toString(), termMatches, relationshipMatches, conditionMatches,
                        changes));
            } else {
                policyRules.add(new ParsedPolicyRule(policyRule.toString(), termMatches, relationshipMatches, conditionMatches,
                        policyRule.errorMessage));
            }
        }
        return new CleanGraphPolicy(policyRules);
    }

    /**
     * A clean instance of a graph policy implementing the parsed rules.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.1.0
     */
    private static class CleanGraphPolicy extends GraphPolicy {
        private final ImmutableList<ParsedPolicyRule> policyRulesChange;
        private final ImmutableList<ParsedPolicyRule> policyRulesError;
        private final Set<String> conditions = new HashSet<String>();

        /**
         * Construct a clean instance of a graph policy.
         * @param policyRules the parsed policy rules
         */
        CleanGraphPolicy(List<ParsedPolicyRule> policyRules) {
            final ImmutableList.Builder<ParsedPolicyRule> policyRulesChangeBuilder = ImmutableList.builder();
            final ImmutableList.Builder<ParsedPolicyRule> policyRulesErrorBuilder = ImmutableList.builder();

            for (final ParsedPolicyRule policyRule : policyRules) {
                if (policyRule.errorMessage == null) {
                    policyRulesChangeBuilder.add(policyRule);
                } else {
                    policyRulesErrorBuilder.add(policyRule);
                }
            }

            this.policyRulesChange = policyRulesChangeBuilder.build();
            this.policyRulesError = policyRulesErrorBuilder.build();
        }

        /**
         * Construct a clean instance of a graph policy.
         * @param policyRulesChange the parsed policy rules whose consequence is graph node state changes
         * @param policyRulesError the parsed policy rules whose consequence is an error condition
         */
        private CleanGraphPolicy(ImmutableList<ParsedPolicyRule> policyRulesChange,
                ImmutableList<ParsedPolicyRule> policyRulesError) {
            this.policyRulesChange = policyRulesChange;
            this.policyRulesError = policyRulesError;
        }

        @Override
        public GraphPolicy getCleanInstance() {
            return new CleanGraphPolicy(policyRulesChange, policyRulesError);
        }

        @Override
        public void setCondition(String name) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("set graph policy condition: " + name);
            }
            conditions.add(name);
        }

        @Override
        public boolean isCondition(String name) {
            return conditions.contains(name);
        }

        @Override
        public Set<Details> review(Map<String, Set<Details>> linkedFrom,
                Details rootObject, Map<String, Set<Details>> linkedTo,
                Set<String> notNullable, boolean isErrorRules) throws GraphException {
            final Set<Details> changedObjects = new HashSet<Details>();
            for (final ParsedPolicyRule policyRule : isErrorRules ? policyRulesError : policyRulesChange) {
                boolean conditionsSatisfied = true;
                for (final ConditionMatch matcher : policyRule.conditionMatchers) {
                    if (matcher.set != isCondition(matcher.name)) {
                        conditionsSatisfied = false;
                        break;
                    }
                }
                if (conditionsSatisfied) {
                    if (policyRule.termMatchers.size() + policyRule.relationshipMatchers.size() == 1) {
                        reviewWithSingleMatch(linkedFrom, rootObject, linkedTo, notNullable, policyRule, changedObjects);
                    } else {
                        reviewWithManyMatches(linkedFrom, rootObject, linkedTo, notNullable, policyRule, changedObjects);
                    }
                }
            }
            return changedObjects;
        }

        /**
         * If there is only a single match, the policy rule may apply multiple times to the root object,
         * through applying to multiple properties or to collection properties.
         * @param linkedFrom details of the objects linking to the root object, by property
         * @param rootObject details of the root objects
         * @param linkedTo details of the objects linked by the root object, by property
         * @param notNullable which properties are not nullable
         * @param policyRule the policy rule to consider applying
         * @param changedObjects the set of details of objects that result from applied changes
         * @throws GraphException if a term named for a change is not defined in the matching
         */
        private void reviewWithSingleMatch(Map<String, Set<Details>> linkedFrom,
                Details rootObject, Map<String, Set<Details>> linkedTo, Set<String> notNullable,
                ParsedPolicyRule policyRule, Set<Details> changedObjects) throws GraphException {
            final SortedMap<String, Details> namedTerms = new TreeMap<String, Details>();
            final MutableBoolean isCheckAllPermissions = new MutableBoolean(true);
            if (!policyRule.termMatchers.isEmpty()) {
                /* apply the term matchers */
                final Set<Details> allTerms = GraphPolicy.allObjects(linkedFrom.values(), rootObject, linkedTo.values());
                for (final TermMatch matcher : policyRule.termMatchers) {
                    for (final Details object : allTerms) {
                        if (matcher.isMatch(predicates, namedTerms, isCheckAllPermissions, object)) {
                            recordChanges(policyRule, changedObjects, namedTerms, isCheckAllPermissions.booleanValue());
                            namedTerms.clear();
                            isCheckAllPermissions.setValue(true);
                        }
                    }
                }
            }
            /* apply the relationship matchers */
            for (final RelationshipMatch matcher : policyRule.relationshipMatchers) {
                /* consider the root object as the linked object */
                for (final Entry<String, Set<Details>> dataPerProperty : linkedFrom.entrySet()) {
                    final String classProperty = dataPerProperty.getKey();
                    final boolean isNotNullable = notNullable.contains(classProperty);
                    for (final Details linkerObject : dataPerProperty.getValue()) {
                        if (matcher.isMatch(predicates, namedTerms, isCheckAllPermissions,
                                linkerObject, rootObject, classProperty, isNotNullable)) {
                            recordChanges(policyRule, changedObjects, namedTerms, isCheckAllPermissions.booleanValue());
                            namedTerms.clear();
                            isCheckAllPermissions.setValue(true);
                        }
                    }
                }
                /* consider the root object as the linker object */
                for (final Entry<String, Set<Details>> dataPerProperty : linkedTo.entrySet()) {
                    final String classProperty = dataPerProperty.getKey();
                    final boolean isNotNullable = notNullable.contains(classProperty);
                    for (final Details linkedObject : dataPerProperty.getValue()) {
                        if (matcher.isMatch(predicates, namedTerms, isCheckAllPermissions,
                                rootObject, linkedObject, classProperty, isNotNullable)) {
                            recordChanges(policyRule, changedObjects, namedTerms, isCheckAllPermissions.booleanValue());
                            namedTerms.clear();
                            isCheckAllPermissions.setValue(true);
                        }
                    }
                }
            }
        }

        /**
         * If there are multiple matches, the policy rule may apply only once to the root object.
         * Terms named in any of the matches may be used in any of the changes.
         * @param linkedFrom details of the objects linking to the root object, by property
         * @param rootObject details of the root objects
         * @param linkedTo details of the objects linked by the root object, by property
         * @param notNullable which properties are not nullable
         * @param policyRule the policy rule to consider applying
         * @param changedObjects the set of details of objects that result from applied changes
         * @throws GraphException if a term named for a change is not defined in the matching
         */
        private void reviewWithManyMatches(Map<String, Set<Details>> linkedFrom,
                Details rootObject, Map<String, Set<Details>> linkedTo, Set<String> notNullable,
                ParsedPolicyRule policyRule, Set<Details> changedObjects) throws GraphException {
            final SortedMap<String, Details> namedTerms = new TreeMap<String, Details>();
            final MutableBoolean isCheckAllPermissions = new MutableBoolean(true);
            final Set<TermMatch> unmatchedTerms = new HashSet<TermMatch>(policyRule.termMatchers);
            final Set<Details> allTerms = unmatchedTerms.isEmpty() ? Collections.<Details>emptySet()
                    : GraphPolicy.allObjects(linkedFrom.values(), rootObject, linkedTo.values());
            final Set<RelationshipMatch> unmatchedRelationships = new HashSet<RelationshipMatch>(policyRule.relationshipMatchers);
            /* try all the matchers against all the terms */
            do {
                final int namedTermCount = namedTerms.size();
                Iterator<TermMatch> unmatchedTermIterator;
                Iterator<RelationshipMatch> unmatchedRelationshipIterator;
                /* apply the term matchers */
                unmatchedTermIterator = unmatchedTerms.iterator();
                while (unmatchedTermIterator.hasNext()) {
                    final TermMatch matcher = unmatchedTermIterator.next();
                    for (final Details object : allTerms) {
                        if (matcher.isMatch(predicates, namedTerms, isCheckAllPermissions, object)) {
                            unmatchedTermIterator.remove();
                        }
                    }
                }
                /* consider the root object as the linked object */
                for (final Entry<String, Set<Details>> dataPerProperty : linkedFrom.entrySet()) {
                    final String classProperty = dataPerProperty.getKey();
                    final boolean isNotNullable = notNullable.contains(classProperty);
                    for (final Details linkerObject : dataPerProperty.getValue()) {
                        unmatchedTermIterator = unmatchedTerms.iterator();
                        while (unmatchedTermIterator.hasNext()) {
                            final TermMatch matcher = unmatchedTermIterator.next();
                            if (matcher.isMatch(predicates, namedTerms, isCheckAllPermissions, linkerObject)) {
                                unmatchedTermIterator.remove();
                            }
                        }
                        unmatchedRelationshipIterator = unmatchedRelationships.iterator();
                        while (unmatchedRelationshipIterator.hasNext()) {
                            final RelationshipMatch matcher = unmatchedRelationshipIterator.next();
                            if (matcher.isMatch(predicates, namedTerms, isCheckAllPermissions,
                                    linkerObject, rootObject, classProperty, isNotNullable)) {
                                unmatchedRelationshipIterator.remove();
                            }
                        }
                    }
                }
                /* consider the root object as the linker object */
                for (final Entry<String, Set<Details>> dataPerProperty : linkedTo.entrySet()) {
                    final String classProperty = dataPerProperty.getKey();
                    final boolean isNotNullable = notNullable.contains(classProperty);
                    for (final Details linkedObject : dataPerProperty.getValue()) {
                        unmatchedTermIterator = unmatchedTerms.iterator();
                        while (unmatchedTermIterator.hasNext()) {
                            final TermMatch matcher = unmatchedTermIterator.next();
                            if (matcher.isMatch(predicates, namedTerms, isCheckAllPermissions, linkedObject)) {
                                unmatchedTermIterator.remove();
                            }
                        }
                        unmatchedRelationshipIterator = unmatchedRelationships.iterator();
                        while (unmatchedRelationshipIterator.hasNext()) {
                            final RelationshipMatch matcher = unmatchedRelationshipIterator.next();
                            if (matcher.isMatch(predicates, namedTerms, isCheckAllPermissions,
                                    rootObject, linkedObject, classProperty, isNotNullable)) {
                                unmatchedRelationshipIterator.remove();
                            }
                        }
                    }
                }
                /* match relationships among existing terms without any property link via the root object */
                unmatchedRelationshipIterator = unmatchedRelationships.iterator();
                while (unmatchedRelationshipIterator.hasNext()) {
                    final RelationshipMatch matcher = unmatchedRelationshipIterator.next();
                    if (matcher.propertyName != null || matcher.notNullable != null) continue;
                    final String leftTermName = matcher.getExistingLeftTerm();
                    if (leftTermName == null) continue;
                    final String rightTermName = matcher.getExistingRightTerm();
                    if (rightTermName == null) continue;
                    final Details leftDetails = namedTerms.get(leftTermName);
                    if (leftDetails == null) continue;
                    final Details rightDetails = namedTerms.get(rightTermName);
                    if (rightDetails == null) continue;
                    if (matcher.isMatch(predicates, namedTerms, isCheckAllPermissions, leftDetails, rightDetails, null, false)) {
                        unmatchedRelationshipIterator.remove();
                    }
                }
                if (unmatchedTerms.isEmpty() && unmatchedRelationships.isEmpty()) {
                    /* success, all matched */
                    recordChanges(policyRule, changedObjects, namedTerms, isCheckAllPermissions.booleanValue());
                    return;
                } else if (namedTerms.size() == namedTermCount) {
                    /* failure, all matchers will fail on retry */
                    return;
                }
            } while (true);
        }

        /**
         * Effect the changes.
         * @param policyRule the policy rule that is now to be effected
         * @param changedObjects the objects affected by the policy rules (to be updated by this method)
         * @param namedTerms the name dictionary of matched terms
         * @param isCheckAllPermissions if permissions are to be checked for all of the matched objects
         * @throws GraphException if a term to change is one not named among the policy rule's matchers,
         * or if the policy rule's consequence is itself an error condition
         */
        private static void recordChanges(ParsedPolicyRule policyRule, Set<Details> changedObjects,
                Map<String, Details> namedTerms, boolean isCheckAllPermissions) throws GraphException {
            final StringBuffer logMessage;
            if (LOGGER != null && LOGGER.isDebugEnabled()) {
                /* log applicable rule match and old status of terms */
                logMessage = new StringBuffer();
                logMessage.append("matched ");
                logMessage.append(policyRule.asString);
                logMessage.append(", where ");
                for (final Entry<String, Details> namedTerm : namedTerms.entrySet()) {
                    logMessage.append(namedTerm.getKey());
                    logMessage.append(" is ");
                    logMessage.append(namedTerm.getValue());
                    logMessage.append(", ");
                }
            } else {
                /* not logging rule matches */
                logMessage = null;
            }
            if (policyRule.errorMessage != null) {
                /* throw the error that is this rule's consequence */
                String message = policyRule.errorMessage;
                for (final Entry<String, Details> namedTerm : namedTerms.entrySet()) {
                    /* expand each named term to its actual match */
                    final String termName = namedTerm.getKey();
                    final IObject termMatch = namedTerm.getValue().subject;
                    message = message.replace("{" + termName + "}",
                            termMatch.getClass().getSimpleName() + '[' + termMatch.getId() + ']');
                }
                if (logMessage != null) {
                    /* log error rule match */
                    logMessage.append("error thrown");
                    LOGGER.debug(logMessage.toString());
                }
                throw new GraphException(message);
            }
            /* note the new changes to the terms */
            final Map<Change, Details> changedTerms = new HashMap<Change, Details>();
            for (final Change change : policyRule.changes) {
                changedTerms.put(change, change.toChanged(namedTerms));
            }
            /* a permissions override on any match propagates to all truly changed terms */
            if (!isCheckAllPermissions) {
                for (final Entry<Change, Details> changedTerm : changedTerms.entrySet()) {
                    if (changedTerm.getKey().isEffectiveChange()) {
                        changedTerm.getValue().isCheckPermissions = false;
                    }
                }
            }
            if (logMessage != null) {
                /* log new status of terms */
                logMessage.append("making ");
                logMessage.append(Joiner.on(", ").join(changedTerms.values()));
                LOGGER.debug(logMessage.toString());
            }
            changedObjects.addAll(changedTerms.values());
        }
    }
}

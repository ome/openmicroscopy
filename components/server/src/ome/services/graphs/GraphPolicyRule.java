/*
 * Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * A graph policy rule specifies a component of a {@link GraphPolicy}.
 * It is designed to be conveniently created using Spring by supplying configuration metadata to the bean container.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.0.x TODO
 */
public class GraphPolicyRule {
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphPolicyRule.class);

    private static final Pattern NEW_TERM_PATTERN =
            Pattern.compile("(\\w+\\:)?(\\!?[\\w]+)?(\\[\\!?[EDIO]+\\])?(\\{\\!?[iroa]+\\})?");
    private static final Pattern EXISTING_TERM_PATTERN = Pattern.compile("(\\w+)");
    private static final Pattern CHANGE_PATTERN = Pattern.compile("(\\w+\\:)(\\[[EDIO\\*]\\])?(\\{[iroa]\\})?");

    private List<String> matches = Collections.emptyList();
    private List<String> changes = Collections.emptyList();

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

    @Override
    public String toString() {
        return Joiner.on(", ").join(matches) + " to " + Joiner.on(", ").join(changes);
    }

    /**
     * Matches model object instances term on either side of a link among objects.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.0.x TODO
     */
    private static interface TermMatch {
        /**
         * If this matches the given term. Will not adjust {@code namedTerms} unless the match succeeds.
         * @param namedTerms the name dictionary of matched terms (to be updated by this method)
         * @param details the details of the term
         * @return if the term matches
         */
        boolean isMatch(Map<String, Details> namedTerms, Details details);
    }

    /**
     * {@inheritDoc}
     * Matches an existing named term.
     */
    private static class ExistingTermMatch implements TermMatch {
        private final String termName;

        /**
         * Construct an existing term match.
         * @param termName the name of the existing term
         */
        ExistingTermMatch(String termName) {
            this.termName = termName;
        }

        public boolean isMatch(Map<String, Details> namedTerms, Details details) {
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

        /**
         * Construct a new term match. All arguments may be {@code null}.
         * @param termName the name of the term, so as to allow references to it
         * @param requiredClass a class of which the object may be an instance
         * @param prohibitedClass a class of which the object may not be an instance
         * @param permittedActions the actions permitted for the object (assumed to be only {@link GraphPolicy.Action#EXCLUDE}
         * if {@code permittedOrphans} is non-{@code null})
         * @param permittedOrphans the orphan statuses permitted for the object
         */
        NewTermMatch(String termName, Class<? extends IObject> requiredClass, Class<? extends IObject> prohibitedClass,
                Collection<GraphPolicy.Action> permittedActions, Collection<GraphPolicy.Orphan> permittedOrphans) {
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
        }

        public boolean isMatch(Map<String, Details> namedTerms, Details details) {
            final Class<? extends IObject> subjectClass = details.subject.getClass();
            if ((requiredClass == null || requiredClass.isAssignableFrom(subjectClass)) &&
                (prohibitedClass == null || !prohibitedClass.isAssignableFrom(subjectClass)) &&
                permittedActions.contains(details.action) &&
                (details.action != GraphPolicy.Action.EXCLUDE || permittedOrphans.contains(details.orphan))) {
                if (termName == null) {
                    return true;
                } else {
                    final Details oldDetails = namedTerms.get(termName);
                    if (oldDetails == null) {
                        namedTerms.put(termName, details);
                        return true;
                    } else if (oldDetails.equals(details)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    /**
     * Matches relationships between a pair of linked model object instance terms.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.0.x TODO
     */
    private static class RelationshipMatch {
        private final TermMatch leftTerm;
        private final TermMatch rightTerm;
        private final String propertyName;
        private final Boolean notNullable;

        /**
         * Construct a new relationship match.
         * @param leftTerm the match for the left term (the object doing the linking)
         * @param rightTerm the match for the right term (the linked object)
         * @param propertyName the name of the property of the left term that has the right term as its value
         * @param notNullable if the property is not nullable (or {@code null} if either is permitted)
         */
        RelationshipMatch(TermMatch leftTerm, TermMatch rightTerm, String propertyName, Boolean notNullable) {
            this.leftTerm = leftTerm;
            this.rightTerm = rightTerm;
            this.propertyName = propertyName == null ? null : '.' + propertyName;
            this.notNullable = notNullable;
        }

        /**
         * If this matches the given relationship. Will not adjust {@code namedTerms} unless the match succeeds.
         * @param namedTerms the name dictionary of matched terms (to be updated by this method)
         * @param leftDetails the details of the left term, holding the property
         * @param rightDetails the details of the right term, being a value of the property
         * @param classProperty the name of the declaring class and property
         * @param notNullable if the property is not nullable
         * @return if the relationship matches
         */
        boolean isMatch(Map<String, Details> namedTerms, Details leftDetails, Details rightDetails,
                String classProperty, boolean notNullable) {
            if ((this.notNullable != null && this.notNullable != notNullable) ||
                (this.propertyName != null && !classProperty.endsWith(propertyName))) {
                return false;
            }
            final Map<String, Details> newNamedTerms = new HashMap<String, Details>(namedTerms);
            final boolean isMatch = leftTerm.isMatch(newNamedTerms, leftDetails) && rightTerm.isMatch(newNamedTerms, rightDetails);
            if (isMatch) {
                namedTerms.putAll(newNamedTerms);
            }
            return isMatch;
        }
    }

    /**
     * A change to effect if a rule's matchers match.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.0.x TODO
     */
    private static class Change {
        private final String namedTerm;
        private final GraphPolicy.Action action;
        private final GraphPolicy.Orphan orphan;

        /**
         * Construct a change instance.
         * @param namedTerm the term to affect
         * @param action the effect to have on the action, {@code null} for no effect
         * @param orphan the effect to have on the orphan status, {@code null} for no effect
         */
        Change(String namedTerm, GraphPolicy.Action action, GraphPolicy.Orphan orphan) {
            this.namedTerm = namedTerm;
            this.action = action;
            this.orphan = orphan;
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
            return details;
        }
    }

    /**
     * A policy rule with matchers and changes that can now be applied having been parsed from the text-based configuration.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.0.x TODO
     */
    private static class ParsedPolicyRule {
        final String asString;
        final List<TermMatch> termMatchers;
        final List<RelationshipMatch> relationshipMatchers;
        final List<Change> changes;

        /**
         * Construct a policy rule.
         * @param asString a String representation of this rule,
         * recognizably corresponding to its original text-based configuration.
         * @param termMatchers the term matchers that must apply if the changes are to be applied
         * @param relationshipMatchers the relationship matchers that must apply if the changes are to be applied
         * @param changes the effects of this rule, guarded by the matchers
         */
        ParsedPolicyRule(String asString, List<TermMatch> termMatchers, List<RelationshipMatch> relationshipMatchers,
                List<Change> changes) {
            this.asString = asString;
            this.termMatchers = termMatchers;
            this.relationshipMatchers = relationshipMatchers;
            this.changes = changes;
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

        /* parse term name, if any */

        final String termName;
        final Class<? extends IObject> requiredClass;
        final Class<? extends IObject> prohibitedClass;
        final Collection<GraphPolicy.Action> permittedActions;
        final Collection<GraphPolicy.Orphan> permittedOrphans;

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
        } else {
            requiredClass = graphPathBean.getClassForSimpleName(classNameGroup);
            prohibitedClass = null;
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

        return new NewTermMatch(termName, requiredClass, prohibitedClass, permittedActions, permittedOrphans);
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
        return new RelationshipMatch(leftTermMatch, rightTermMatch, propertyName, notNullable);
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

        return new Change(termName, action, orphan);
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
        /* parse the rules */
        final List<ParsedPolicyRule> policyRules = new ArrayList<ParsedPolicyRule>();
        for (final GraphPolicyRule policyRule : rules) {
            final List<TermMatch> termMatches = new ArrayList<TermMatch>();
            final List<RelationshipMatch> relationshipMatches = new ArrayList<RelationshipMatch>();
            final List<Change> changes = new ArrayList<Change>();
            for (final String match : policyRule.matches) {
                final String[] words = match.trim().split("\\s+");
                if (words.length == 1) {
                    termMatches.add(parseTermMatch(graphPathBean, words[0]));
                } else if (words.length == 3) {
                    relationshipMatches.add(parseRelationshipMatch(graphPathBean, words[0], words[1], words[2]));
                } else {
                    throw new GraphException("failed to parse match " + match);
                }
            }
            for (final String change : policyRule.changes) {
                changes.add(parseChange(change.trim()));
            }
            policyRules.add(new ParsedPolicyRule(policyRule.toString(), termMatches, relationshipMatches, changes));
        }
        /* construct the graph policy */
        return new GraphPolicy() {
            @Override
            public Set<Details> review(Map<String, Set<Details>> linkedFrom,
                    Details rootObject, Map<String, Set<Details>> linkedTo,
                    Set<String> notNullable) throws GraphException {
                final Set<Details> changedObjects = new HashSet<Details>();
                for (final ParsedPolicyRule policyRule : policyRules) {
                    if (policyRule.termMatchers.size() + policyRule.relationshipMatchers.size() == 1) {
                        reviewWithSingleMatch(linkedFrom, rootObject, linkedTo, notNullable, policyRule, changedObjects);
                    } else {
                        reviewWithManyMatches(linkedFrom, rootObject, linkedTo, notNullable, policyRule, changedObjects);
                    }
                }
                return changedObjects;
            }
        };
    }

    /**
     * If there is only a single relationship match, the policy rule may apply multiple times to the root object,
     * through applying to multiple properties or to collection properties.
     * @param linkedFrom details of the objects linking to the root object, by property
     * @param rootObject details of the root objects
     * @param linkedTo details of the objects linked by the root object, by property
     * @param notNullable which properties are not nullable
     * @param policyRule the policy rule to consider applying
     * @param changedObjects the set of details of objects that result from applied changes
     * @throws GraphException if a term named for a change is not defined in the matching
     */
    private static void reviewWithSingleMatch(Map<String, Set<Details>> linkedFrom,
            Details rootObject, Map<String, Set<Details>> linkedTo, Set<String> notNullable,
            ParsedPolicyRule policyRule, Set<Details> changedObjects) throws GraphException {
        final SortedMap<String, Details> namedTerms = new TreeMap<String, Details>();
        if (!policyRule.termMatchers.isEmpty()) {
            /* apply the term matchers */
            final Set<Details> allTerms = new HashSet<Details>();
            allTerms.add(rootObject);
            for (final Entry<String, Set<Details>> dataPerProperty : linkedFrom.entrySet()) {
                allTerms.addAll(dataPerProperty.getValue());
            }
            for (final Entry<String, Set<Details>> dataPerProperty : linkedTo.entrySet()) {
                allTerms.addAll(dataPerProperty.getValue());
            }
            for (final TermMatch matcher : policyRule.termMatchers) {
                for (final Details object : allTerms) {
                    if (matcher.isMatch(namedTerms, object)) {
                        recordChanges(policyRule, changedObjects, namedTerms);
                        namedTerms.clear();
                    }
                }
            }
        }
        /* apply the relationship matchers */
        for (final RelationshipMatch matcher : policyRule.relationshipMatchers) {
            /* consider the root object as the linked object */
            for (final Entry<String, Set<Details>> dataPerProperty : linkedFrom.entrySet()) {
                final String classProperty = dataPerProperty.getKey();
                final boolean isNotNullable = notNullable.contains(dataPerProperty.getKey());
                for (final Details linkerObject : dataPerProperty.getValue()) {
                    if (matcher.isMatch(namedTerms, linkerObject, rootObject, classProperty, isNotNullable)) {
                        recordChanges(policyRule, changedObjects, namedTerms);
                        namedTerms.clear();
                    }
                }
            }
            /* consider the root object as the linker object */
            for (final Entry<String, Set<Details>> dataPerProperty : linkedTo.entrySet()) {
                final String classProperty = dataPerProperty.getKey();
                final boolean isNotNullable = notNullable.contains(dataPerProperty.getKey());
                for (final Details linkedObject : dataPerProperty.getValue()) {
                    if (matcher.isMatch(namedTerms, rootObject, linkedObject, classProperty, isNotNullable)) {
                        recordChanges(policyRule, changedObjects, namedTerms);
                        namedTerms.clear();
                    }
                }
            }
        }
    }

    /**
     * If there are multiple relationship matches, the policy rule may apply only once to the root object.
     * Terms named in any of the matches may be used in any of the changes.
     * @param linkedFrom details of the objects linking to the root object, by property
     * @param rootObject details of the root objects
     * @param linkedTo details of the objects linked by the root object, by property
     * @param notNullable which properties are not nullable
     * @param policyRule the policy rule to consider applying
     * @param changedObjects the set of details of objects that result from applied changes
     * @throws GraphException if a term named for a change is not defined in the matching
     */
    private static void reviewWithManyMatches(Map<String, Set<Details>> linkedFrom,
            Details rootObject, Map<String, Set<Details>> linkedTo, Set<String> notNullable,
            ParsedPolicyRule policyRule, Set<Details> changedObjects) throws GraphException {
        final SortedMap<String, Details> namedTerms = new TreeMap<String, Details>();
        final Set<TermMatch> unmatchedTerms = new HashSet<TermMatch>(policyRule.termMatchers);
        final Set<RelationshipMatch> unmatchedRelationships = new HashSet<RelationshipMatch>(policyRule.relationshipMatchers);
        for (final TermMatch matcher : unmatchedTerms) {
            if (matcher.isMatch(namedTerms, rootObject)) {
                unmatchedTerms.remove(matcher);
            }
        }
        /* consider the root object as the linked object */
        for (final Entry<String, Set<Details>> dataPerProperty : linkedFrom.entrySet()) {
            final String classProperty = dataPerProperty.getKey();
            final boolean isNotNullable = notNullable.contains(dataPerProperty.getKey());
            for (final Details linkerObject : dataPerProperty.getValue()) {
                for (final TermMatch matcher : unmatchedTerms) {
                    if (matcher.isMatch(namedTerms, linkerObject)) {
                        unmatchedTerms.remove(matcher);
                    }
                }
                final Iterator<RelationshipMatch> unmatchedIterator = unmatchedRelationships.iterator();
                while (unmatchedIterator.hasNext()) {
                    final RelationshipMatch matcher = unmatchedIterator.next();
                    if (matcher.isMatch(namedTerms, linkerObject, rootObject, classProperty, isNotNullable)) {
                        unmatchedIterator.remove();
                    }
                }
            }
        }
        /* consider the root object as the linker object */
        for (final Entry<String, Set<Details>> dataPerProperty : linkedTo.entrySet()) {
            final String classProperty = dataPerProperty.getKey();
            final boolean isNotNullable = notNullable.contains(dataPerProperty.getKey());
            for (final Details linkedObject : dataPerProperty.getValue()) {
                for (final TermMatch matcher : unmatchedTerms) {
                    if (matcher.isMatch(namedTerms, linkedObject)) {
                        unmatchedTerms.remove(matcher);
                    }
                }
                final Iterator<RelationshipMatch> unmatchedIterator = unmatchedRelationships.iterator();
                while (unmatchedIterator.hasNext()) {
                    final RelationshipMatch matcher = unmatchedIterator.next();
                    if (matcher.isMatch(namedTerms, rootObject, linkedObject, classProperty, isNotNullable)) {
                        unmatchedIterator.remove();
                    }
                }
            }
        }
        if (unmatchedTerms.isEmpty() && unmatchedRelationships.isEmpty()) {
            recordChanges(policyRule, changedObjects, namedTerms);
        }
    }

    /**
     * Effect the changes.
     * @param policyRule the policy rule that is now to be effected
     * @param changedObjects the objects affected by the policy rules (to be updated by this method)
     * @param namedTerms
     * @throws GraphException
     */
    private static void recordChanges(ParsedPolicyRule policyRule, Set<Details> changedObjects,
            Map<String, Details> namedTerms) throws GraphException {
        if (LOGGER != null && LOGGER.isDebugEnabled()) {
            final StringBuffer sb = new StringBuffer();
            sb.append("matched ");
            sb.append(policyRule.asString);
            sb.append(", where ");
            for (final Entry<String, Details> namedTerm : namedTerms.entrySet()) {
                sb.append(namedTerm.getKey());
                sb.append(" is ");
                sb.append(namedTerm.getValue());
                sb.append(", ");
            }
            sb.append("making ");
            final List<String> newValues = new ArrayList<String>();
            for (final Change change : policyRule.changes) {
                final Details newValue = change.toChanged(namedTerms);
                newValues.add(newValue.toString());
                changedObjects.add(newValue);
            }
            sb.append(Joiner.on(", ").join(newValues));
            LOGGER.debug(sb.toString());
        } else {
            for (final Change change : policyRule.changes) {
                changedObjects.add(change.toChanged(namedTerms));
            }
        }
    }
}

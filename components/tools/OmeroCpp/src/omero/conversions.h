/*
 * Copyright (C) 2015 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

#ifndef OMERO_CONVERSIONS_H
#define OMERO_CONVERSIONS_H

#include <omero/IceNoWarnPush.h>
#include <Ice/Ice.h>
#include <omero/IceNoWarnPop.h>

#include <IceUtil/Config.h>
#include <Ice/Handle.h>
#include <vector>

#ifndef OMERO_CLIENT
#   ifdef OMERO_CLIENT_EXPORTS
#       define OMERO_CLIENT ICE_DECLSPEC_EXPORT
#   else
#       define OMERO_CLIENT ICE_DECLSPEC_IMPORT
#   endif
#endif


namespace omero {
  namespace conversion_types {
    class Conversion;
    typedef IceUtil::Handle<Conversion> ConversionPtr;
  }
}

namespace IceInternal {
  OMERO_CLIENT ::Ice::LocalObject* upCast(::omero::conversion_types::Conversion*);
}

namespace omero {

  namespace conversion_types {

    /**
     * Base-functor like object which can be used for preparing complex
     * equations for converting from one unit to another. Primarily these
     * classes and static methods are used via code-generation. Sympy-generated
     * strings are placed directly into code. If the proper imports are in place,
     * then a top-level {@link Conversion} (usually of type {@link Add} or
     * {@link Mul} is returned from the evaluation.
     */
    class OMERO_CLIENT Conversion : public IceUtil::Shared {

    private:
        Conversion& operator=(const Conversion& rv);
        Conversion(Conversion&);

    protected:

        /**
         * Conversions, if any, which are passed into the constructor
         * of this instance. If none are passed, then the implementation
         * has a short-cut form, e.g. taking an {@link Integer} rather than
         * an {@link Int}.
         */
        std::vector<ConversionPtr> conversions;

    public:

        /**
         * Constructor for two {@link Conversion} objects. No processing
         * happens during constructor. Instead, the {@link #convert(double)}
         * method will handle descending through the recursive structure.
         */
        Conversion(const ConversionPtr& c1, const ConversionPtr& c2);

        /**
         * Constructor for one {@link Conversion} object. No processing
         * happens during constructor. Instead, the {@link #convert(double)}
         * method will handle descending through the recursive structure.
         */
        Conversion(const ConversionPtr& c1);

        /**
         * Constructor which can be fallen back on when no
         * Conversion instances are passed.
         **/
        Conversion();

        virtual ~Conversion();

        /**
         * Primary operator for {@link Conversion} instances.
         * @param original A unit value which is to be processed through the
         *    tree-like representation of this equation. Only {@link Sym} objects
         *    will actually use the "original" value.
         * @return a {@link BigDecimal} result from the calculation. If this value
         *    maps to {@link Double#NEGATIVE_INFINITY} or
         *    {@link Double#POSITIVE_INFINITY}, then a {@link BigResult} exception
         *    should be thrown before returning to clients.
         */
        virtual double convert(double original) = 0;

    };

    /**
     * Sums all {@link Conversion} instances via {@link BigDecimal#add(BigDecimal)}.
     */
    class OMERO_CLIENT Add : virtual public Conversion {

    private:
        Add& operator=(const Add& rv);
        Add(Add&);

    public:
        Add(const ConversionPtr& c1, const ConversionPtr& c2);
        double convert(double original);
    };

    /**
     * Simply is a representation of a possibly large integer.
     */
    class OMERO_CLIENT Int : virtual public Conversion {

    private:
        Int& operator=(const Int& rv);
        Int(Int&);

    private:
        long i;
        std::string s;

    public:
        Int(long i);
        Int(std::string s);
        double convert(double original);
    };

    /**
     * Multiplies all {@link Conversion} instances via
     * {@link BigDecimal#multiply(BigDecimal)}.
     */
    class OMERO_CLIENT Mul : virtual public Conversion {

    private:
        Mul& operator=(const Mul& rv);
        Mul(Mul&);

    public:
        Mul(const ConversionPtr& c1, const ConversionPtr& c2);
        double convert(double original);
    };

    /**
     * Exponentiates two {@link Conversion} instances via
     * {@link BigDecimal#pow(BigDecimal)}.
     */
    class OMERO_CLIENT Pow : virtual public Conversion {

    private:
        Pow& operator=(const Pow& rv);
        Pow(Pow&);

    private:
        long base;
        int exp;

    public:
        Pow(long base, int exp);
        double convert(double original);

    };

    /**
     * Divides two {@link Conversion} instances via
     * {@link BigDecimal#divide(BigDecimal, MathContext)}.
     */
    class OMERO_CLIENT Rat : virtual public Conversion {

    private:
        Rat& operator=(const Rat& rv);
        Rat(Rat&);

    private:
        long num;
        long denom;
        bool delay;

    public:
        Rat(long num, long denom);
        Rat(const ConversionPtr& c1, const ConversionPtr& c2);
        double convert(double original);

    };

    /**
     * Simply represents the variable of the source unit so that
     * {@link Sym#convert(double)} just returns the value passed in.
     */
    class OMERO_CLIENT Sym : virtual public Conversion {

    private:
        Sym& operator=(const Sym& rv);
        Sym(Sym&);

    public:
        Sym(char sym);
        Sym(std::string sym);
        double convert(double original);
    };

  }

  namespace conversions {

    // =======================================================
    // Helper static methods which prevent the need for "new"
    // in the generated code.
    // =======================================================

    /**
     * Static helper for creating {@link Add} instances.
     */
    static inline omero::conversion_types::ConversionPtr Add(
            const omero::conversion_types::ConversionPtr& c1,
            const omero::conversion_types::ConversionPtr& c2) {
        return new omero::conversion_types::Add(c1, c2);
    }

    /**
     * Static helper for creating {@link Int} instances.
     */
    static inline omero::conversion_types::ConversionPtr Int(long i) {
        return new omero::conversion_types::Int(i);
    }

    /**
     * Static helper for creating {@link Int} instances.
     */
    static inline omero::conversion_types::ConversionPtr Int(std::string i) {
        return new omero::conversion_types::Int(i);
    }

    /**
     * Static helper for creating {@link Mul} instances.
     */
    static inline omero::conversion_types::ConversionPtr Mul(
            const omero::conversion_types::ConversionPtr& c1,
            const omero::conversion_types::ConversionPtr& c2) {
        return new omero::conversion_types::Mul(c1, c2);
    }

    /**
     * Static helper for creating {@link Pow} instances.
     */
    static inline omero::conversion_types::ConversionPtr Pow(long num, int den) {
        return new omero::conversion_types::Pow(num, den);
    }

   /**
    * Static helper for creating {@link Rat} instances.
    */
    static inline omero::conversion_types::ConversionPtr Rat(long num, long den) {
        return new omero::conversion_types::Rat(num, den);
    }

    /**
     * Static helper for creating {@link Rat} instances.
     */
    static inline omero::conversion_types::ConversionPtr Rat(
            const omero::conversion_types::ConversionPtr& c1,
            const omero::conversion_types::ConversionPtr& c2) {
        return new omero::conversion_types::Rat(c1, c2);
    }

   /**
    * Static helper for creating {@link Sym} instances.
    */
    static inline omero::conversion_types::ConversionPtr Sym(std::string sym) {
        return new omero::conversion_types::Sym(sym);
    }

  }

}
#endif

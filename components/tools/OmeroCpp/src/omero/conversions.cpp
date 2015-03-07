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

#include <omero/conversions.h>
#include <math.h>

namespace omero {

  namespace conversion_types {

      Conversion::Conversion(const ConversionPtr& c1, const ConversionPtr& c2) {
        conversions.push_back(c1);
        conversions.push_back(c2);
      }

      Conversion::Conversion(const ConversionPtr& c1) {
        conversions.push_back(c1);
      }

      Conversion::Conversion() {

      }

      Conversion::~Conversion() {

      }

      Add::Add(const ConversionPtr& c1, const ConversionPtr& c2)
          : Conversion(c1, c2) {
      }

      double Add::convert(double original) {
          double rv = 0.0;
          std::vector<ConversionPtr>::iterator beg = conversions.begin();
          std::vector<ConversionPtr>::iterator end = conversions.end();
          for(; beg != end; ++beg) {
              rv += (*beg)->convert(original);
          }
          return rv;
      }

      Int::Int(long i) : i(i) {

      }

      Int::Int(std::string s) : s(s) {

      }

      double Int::convert(double) {
          return i;
      }

      Mul::Mul(const ConversionPtr& c1, const ConversionPtr& c2)
          : Conversion(c1, c2) {
      }

      double Mul::convert(double original) {
          double rv = 1.0;
          std::vector<ConversionPtr>::iterator beg = conversions.begin();
          std::vector<ConversionPtr>::iterator end = conversions.end();
          for(; beg != end; ++beg) {
              rv *= (*beg)->convert(original);
          }
          return rv;
      }

      Pow::Pow(long base, int exp) : base(base), exp(exp) {

      }

      double Pow::convert(double original) {
          if (conversions.size() > 0) {
            return pow(
                    conversions[0]->convert(original),
                    conversions[1]->convert(original));
          } else {
            return pow(base, exp);
          }
      }

      Rat::Rat(long num, long denom) : num(num), denom(denom) {

      }

      Rat::Rat(const ConversionPtr& c1, const ConversionPtr& c2) :
          Conversion(c1, c2) {

      }

      double Rat::convert(double original) {
          if (conversions.size() > 0) {
            return conversions[0]->convert(original) /
                   conversions[1]->convert(original);
          } else {
              return static_cast<double>(num) / static_cast<double>(denom);
          }
      }

      Sym::Sym(char) {

      }

      Sym::Sym(std::string) {

      }

      double Sym::convert(double original) {
          return original;
      }

  }

}

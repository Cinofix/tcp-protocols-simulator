/*
 * Class:        RandShiftedPointSet
 * Description:  Point set to which a random shift modulo 1 is applied
 * Environment:  Java
 * Software:     SSJ 
 * Copyright (C) 2001  Pierre L'Ecuyer and Universite de Montreal
 * Organization: DIRO, Universite de Montreal
 * @author       
 * @since
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package umontreal.ssj.hups;

import umontreal.ssj.util.PrintfFormat;
import umontreal.ssj.rng.*;

/**
 * @remark **Adam:** Cette classe reprogramme un `addRandomShift`
 * explicitement sur l’ensemble @f$P@f$, alors que le `ContainerPointSet`
 * applique le `addRandomShift` sur le contenu @f$P@f$, ce qui est beaucoup
 * plus propre et plus général. Faut-il éliminer cette classe? Est-elle
 * vraiment nécessaire?
 *
 *  This container class embodies a point set to which a random shift modulo
 * 1 is applied (i.e., a single uniform random point is added to all points,
 * modulo 1, to randomize the inner point set). When calling
 * #addRandomShift, a new random shift will be generated. This shift is
 * represented by a vector of @f$d@f$ uniforms over @f$(0,1)@f$, where
 * @f$d@f$ is the current dimension of the shift.
 *
 * <div class="SSJ-bigskip"></div><div class="SSJ-bigskip"></div>
 */
public class RandShiftedPointSet extends ContainerPointSet {

   protected double[] shift;           // The random shift.
   protected int dimShift = 0;         // Current dimension of the shift.
   protected int capacityShift = 0;    // Number of array elements;
                                       // always >= dimShift.
   protected RandomStream shiftStream; // Used to generate random shifts.

   /**
    * Constructs a structure to contain a randomly shifted version of `P`.
    * The random shifts will be generated in up to `dimShift` dimensions,
    * using stream `stream`.
    *  @param P            point set being randomized
    *  @param dimShift     dimension of the initial shift
    *  @param stream       stream used for generating random shifts
    */
   public RandShiftedPointSet (PointSet P, int dimShift, RandomStream stream) {
      init (P);
      if (dimShift <= 0) {
         throw new IllegalArgumentException (
            "Cannot construct RandShiftedPointSet with dimShift <= 0");
      }
      shiftStream = stream;
      shift = new double [dimShift];
      capacityShift = this.dimShift = dimShift;
   }

   /**
    * Returns the number of dimensions of the current random shift.
    */
   public int getShiftDimension() {
      return dimShift;
   }

   /**
    * Changes the stream used for the random shifts to `stream`, then
    * refreshes the shift for coordinates `d1` to `d2-1`.
    * @remark **Richard:** Il y a 4 méthodes `addRandomShift`. Peut-être
    * faudrait-il en éliminer 2, comme dans `PointSet`.
    */
   public void addRandomShift (int d1, int d2, RandomStream stream) {
      if (null == stream)
         throw new IllegalArgumentException (
              PrintfFormat.NEWLINE +
                  "   Calling addRandomShift with null stream");
      if (stream != shiftStream)
         shiftStream = stream;
      addRandomShift (d1, d2);
   }

   /**
    * Changes the stream used for the random shifts to `stream`, then
    * refreshes all coordinates of the random shift, up to its current
    * dimension.
    */
   public void addRandomShift (RandomStream stream) {
      if (stream != shiftStream)
         shiftStream = stream;
      addRandomShift (0, dimShift);
   }

   /**
    * Refreshes the random shift (generates new uniform values for the
    * random shift coordinates) for coordinates `d1` to `d2-1`.
    */
   @Deprecated
   public void addRandomShift (int d1, int d2) {
      if (d1 < 0 || d1 > d2)
         throw new IllegalArgumentException ("illegal parameter d1 or d2");
      if (d2 > capacityShift) {
         int d3 = Math.max (4, capacityShift);
         while (d2 > d3)
            d3 *= 2;
         double[] temp = new double[d3];
         capacityShift = d3;
         for (int i = 0; i < d1; i++)
            temp[i] = shift[i];
         shift = temp;
      }
      dimShift = d2;
      for (int i = d1; i < d2; i++)
         shift[i] = shiftStream.nextDouble();

 // Just for testing, to see the single uniform random point
 //     for(int k = 0; k < d2; k++)
 //       System.out.println ("shift " + k + " = " + shift[k]);
 //     System.out.println();

   }

   /**
    * Refreshes all coordinates of the random shift, up to its current
    * dimension.
    */
   @Deprecated
   public void addRandomShift() {
      addRandomShift (0, dimShift);
   }


   public String toString() {
      return "RandShiftedPointSet of: {" + PrintfFormat.NEWLINE
              + P.toString() + PrintfFormat.NEWLINE + "}";
   }

   public PointSetIterator iterator() {
      return new RandShiftedPointSetIterator();
   }

   // ***************************************************************

   private class RandShiftedPointSetIterator
                 extends ContainerPointSetIterator {

      public double getCoordinate (int i, int j) {
         int d1 = innerIterator.getCurCoordIndex();
         if (dimShift <= d1)
            // Must extend randomization.
            addRandomShift (dimShift, 1 + d1);
         double u = P.getCoordinate(i, j) + shift[j];
         if (u >= 1.0)
            u -= 1.0;
         if (u > 0.0)
            return u;
         return EpsilonHalf;  // avoid u = 0
      }

      public double nextCoordinate() {
         int d1 = innerIterator.getCurCoordIndex();
         if (dimShift <= d1)
            addRandomShift (dimShift,1 + d1 );
         double u = shift [d1];
         u += innerIterator.nextCoordinate();
         if (u >= 1.0)
            u -= 1.0;
         if (u > 0.0)
            return u;
         return EpsilonHalf;  // avoid u = 0
      }

   }
}
/*
 * Class:        LMScrambleShift
 * Description:  performs a left matrix scramble and adds a random digital shift
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
 import umontreal.ssj.rng.RandomStream;
 import java.lang.IllegalArgumentException;

/**
 * This class implements a  @ref umontreal.ssj.hups.PointSetRandomization
 * that performs a left matrix scrambling and adds a random digital shift.
 * Point set must be a  @ref umontreal.ssj.hups.DigitalNet or an
 * IllegalArgumentException is thrown.
 *
 * <div class="SSJ-bigskip"></div><div class="SSJ-bigskip"></div>
 */
public class LMScrambleShift extends RandomShift {

   /**
    * Empty constructor.
    */
   public LMScrambleShift() {
   }

   /**
    * Sets internal variable `stream` to the given `stream`.
    *  @param stream       stream to use in the randomization
    */
   public LMScrambleShift (RandomStream stream) {
       super(stream);
   }

   /**
    * This method calls
    * umontreal.ssj.hups.DigitalNet.leftMatrixScramble(RandomStream), then
    * umontreal.ssj.hups.DigitalNet.addRandomShift(RandomStream). If `p`
    * is not a  @ref umontreal.ssj.hups.DigitalNet, an
    * IllegalArgumentException is thrown.
    *  @param p            Point set to randomize
    */
   public void randomize (PointSet p) {
      if (p instanceof DigitalNet) {
         ((DigitalNet)p).leftMatrixScramble (stream);
         ((DigitalNet)p).addRandomShift (stream);
      } else {
         throw new IllegalArgumentException("LMScrambleShift"+
                                            " can only randomize a DigitalNet");
      }
   }

}
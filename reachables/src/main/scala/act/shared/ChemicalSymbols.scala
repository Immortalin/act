/*************************************************************************
*                                                                        *
*  This file is part of the 20n/act project.                             *
*  20n/act enables DNA prediction for synthetic biology/bioengineering.  *
*  Copyright (C) 2017 20n Labs, Inc.                                     *
*                                                                        *
*  Please direct all queries to act@20n.com.                             *
*                                                                        *
*  This program is free software: you can redistribute it and/or modify  *
*  it under the terms of the GNU General Public License as published by  *
*  the Free Software Foundation, either version 3 of the License, or     *
*  (at your option) any later version.                                   *
*                                                                        *
*  This program is distributed in the hope that it will be useful,       *
*  but WITHOUT ANY WARRANTY; without even the implied warranty of        *
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
*  GNU General Public License for more details.                          *
*                                                                        *
*  You should have received a copy of the GNU General Public License     *
*  along with this program.  If not, see <http://www.gnu.org/licenses/>. *
*                                                                        *
*************************************************************************/

package act.shared

object ChemicalSymbols {

  sealed trait Atom { def symbol: String; def mass: MonoIsotopicMass; def maxValency: Int }
  case object C extends Atom { val symbol = "C"; val mass = new MonoIsotopicMass(12.000000); val maxValency = 4  }
  case object H extends Atom { val symbol = "H"; val mass = new MonoIsotopicMass( 1.007825); val maxValency = 1  }
  case object O extends Atom { val symbol = "O"; val mass = new MonoIsotopicMass(15.994915); val maxValency = 2  }
  case object N extends Atom { val symbol = "N"; val mass = new MonoIsotopicMass(14.003074); val maxValency = 4  }
  case object P extends Atom { val symbol = "P"; val mass = new MonoIsotopicMass(30.973761); val maxValency = 2  }
  case object S extends Atom { val symbol = "S"; val mass = new MonoIsotopicMass(31.972071); val maxValency = 6  }
  case object I extends Atom { val symbol = "I"; val mass = new MonoIsotopicMass(126.904457); val maxValency = 1 }
  case object F extends Atom { val symbol = "F"; val mass = new MonoIsotopicMass(18.998404); val maxValency = 1 }
  case object Cl extends Atom { val symbol = "Cl"; val mass = new MonoIsotopicMass(34.968853); val maxValency = 1 }
  case object Br extends Atom { val symbol = "Br"; val mass = new MonoIsotopicMass(78.918327); val maxValency = 1 }

  val AllAtoms = List(C, H, O, N, P, S, Cl, Br, I, F)
  
  abstract class AminoAcid {
    def name: String
    def symbol: Char
    def formula: String
    def mass: MonoIsotopicMass
    def elems: Map[Atom, Int]
  }
  
  case object Gly extends AminoAcid {
    val name = "Glycine"; val symbol = 'G'; val formula = "C2H5NO2"; val mass = new MonoIsotopicMass(75.032028);
    val elems: Map[Atom, Int] = Map(C->2, H->5, O->2, N->1, S->0);
    val url = "http://www.chemspider.com/Chemical-Structure.730.html"
  }
  case object Ala extends AminoAcid {
    val name = "Alanine"; val symbol = 'A'; val formula = "C3H7NO2"; val mass = new MonoIsotopicMass(89.047676);
    val elems: Map[Atom, Int] = Map(C->3, H->7, O->2, N->1, S->0);
    val url = "http://www.chemspider.com/Chemical-Structure.582.html"
  }
  case object Pro extends AminoAcid {
    val name = "Proline"; val symbol = 'P'; val formula = "C5H9NO2"; val mass = new MonoIsotopicMass(115.063332);
    val elems: Map[Atom, Int] = Map(C->5, H->9, O->2, N->1, S->0);
    val url = "http://www.chemspider.com/Chemical-Structure.594.html"
  }
  case object Val extends AminoAcid {
    val name = "Valine"; val symbol = 'V'; val formula = "C5H11NO2"; val mass = new MonoIsotopicMass(117.078979);
    val elems: Map[Atom, Int] = Map(C->5, H->11, O->2, N->1, S->0);
    val url = "http://www.chemspider.com/Chemical-Structure.1148.html"
  }
  case object Cys extends AminoAcid {
    val name = "Cysteine"; val symbol = 'C'; val formula = "C3H7NO2S"; val mass = new MonoIsotopicMass(121.019745);
    val elems: Map[Atom, Int] = Map(C->3, H->7, O->2, N->1, S->1);
    val url = "http://www.chemspider.com/Chemical-Structure.574.html"
  }
  case object Ile extends AminoAcid {
    val name = "Isoleucine"; val symbol = 'I'; val formula = "C6H13NO2"; val mass = new MonoIsotopicMass(131.094635);
    val elems: Map[Atom, Int] = Map(C->6, H->13, O->2, N->1, S->0);
    val url = "http://www.chemspider.com/Chemical-Structure.769.html"
  }
  case object Leu extends AminoAcid {
    val name = "Leucine"; val symbol = 'L'; val formula = "C6H13NO2"; val mass = new MonoIsotopicMass(131.094635);
    val elems: Map[Atom, Int] = Map(C->6, H->13, O->2, N->1, S->0);
    val url = "http://www.chemspider.com/Chemical-Structure.834.html"
  }
  case object Met extends AminoAcid {
    val name = "Methionine"; val symbol = 'M'; val formula = "C5H11NO2S"; val mass = new MonoIsotopicMass(149.051056);
    val elems: Map[Atom, Int] = Map(C->5, H->11, O->2, N->1, S->1);
    val url = "http://www.chemspider.com/Chemical-Structure.853.html"
  }
  case object Phe extends AminoAcid {
    val name = "Phenylalanine"; val symbol = 'F'; val formula = "C9H11NO2"; val mass = new MonoIsotopicMass(165.078979);
    val elems: Map[Atom, Int] = Map(C->9, H->11, O->2, N->1, S->0);
    val url = "http://www.chemspider.com/Chemical-Structure.5910.html"
  }
  case object Ser extends AminoAcid {
    val name = "Serine"; val symbol = 'S'; val formula = "C3H7NO3"; val mass = new MonoIsotopicMass(105.042595);
    val elems: Map[Atom, Int] = Map(C->3, H->7, O->3, N->1, S->0);
    val url = "http://www.chemspider.com/Chemical-Structure.597.html"
  }
  case object Thr extends AminoAcid {
    val name = "Threonine"; val symbol = 'T'; val formula = "C4H9NO3"; val mass = new MonoIsotopicMass(119.058243);
    val elems: Map[Atom, Int] = Map(C->4, H->9, O->3, N->1, S->0);
    val url = "http://www.chemspider.com/Chemical-Structure.6051.html"
  }
  case object Tyr extends AminoAcid {
    val name = "Tyrosine"; val symbol = 'Y'; val formula = "C9H11NO3"; val mass = new MonoIsotopicMass(181.073898);
    val elems: Map[Atom, Int] = Map(C->9, H->11, O->3, N->1, S->0);
    val url = "http://www.chemspider.com/Chemical-Structure.5833.html"
  }
  case object Asp extends AminoAcid {
    val name = "Aspartate"; val symbol = 'D'; val formula = "C4H7NO4"; val mass = new MonoIsotopicMass(133.037506);
    val elems: Map[Atom, Int] = Map(C->4, H->7, O->4, N->1, S->0);
    val url = "http://www.chemspider.com/Chemical-Structure.411.html"
  }
  case object Glu extends AminoAcid {
    val name = "Glutamate"; val symbol = 'E'; val formula = "C5H9NO4"; val mass = new MonoIsotopicMass(147.053162);
    val elems: Map[Atom, Int] = Map(C->5, H->9, O->4, N->1, S->0);
    val url = "http://www.chemspider.com/Chemical-Structure.30572.html"
  }
  case object Lys extends AminoAcid {
    val name = "Lysine"; val symbol = 'K'; val formula = "C6H14N2O2"; val mass = new MonoIsotopicMass(146.10553);
    val elems: Map[Atom, Int] = Map(C->6, H->14, O->2, N->2, S->0);
    val url = "http://www.chemspider.com/Chemical-Structure.843.html"
  }
  case object Trp extends AminoAcid {
    val name = "Tryptophan"; val symbol = 'W'; val formula = "C11H12N2O2"; val mass = new MonoIsotopicMass(204.089874);
    val elems: Map[Atom, Int] = Map(C->11, H->12, O->2, N->2, S->0);
    val url = "http://www.chemspider.com/Chemical-Structure.1116.html"
  }
  case object Asn extends AminoAcid {
    val name = "Asparagine"; val symbol = 'N'; val formula = "C4H8N2O3"; val mass = new MonoIsotopicMass(132.053497);
    val elems: Map[Atom, Int] = Map(C->4, H->8, O->3, N->2, S->0);
    val url = "http://www.chemspider.com/Chemical-Structure.231.html"
  }
  case object Gln extends AminoAcid {
    val name = "Glutamine"; val symbol = 'Q'; val formula = "C5H10N2O3"; val mass = new MonoIsotopicMass(146.069138);
    val elems: Map[Atom, Int] = Map(C->5, H->10, O->3, N->2, S->0);
    val url = "http://www.chemspider.com/Chemical-Structure.718.html"
  }
  case object His extends AminoAcid {
    val name = "Histidine"; val symbol = 'H'; val formula = "C6H9N3O2"; val mass = new MonoIsotopicMass(155.069473);
    val elems: Map[Atom, Int] = Map(C->6, H->9, O->2, N->3, S->0);
    val url = "http://www.chemspider.com/Chemical-Structure.6038.html"
  }
  case object Arg extends AminoAcid {
    val name = "Arginine"; val symbol = 'R'; val formula = "C6H14N4O2"; val mass = new MonoIsotopicMass(174.111679);
    val elems: Map[Atom, Int] = Map(C->6, H->14, O->2, N->4, S->0);
    val url = "http://www.chemspider.com/Chemical-Structure.227.html"
  }

  val AllAminoAcids = List(Gly, Ala, Pro, Val, Cys, Ile, Leu, Met, Phe, Ser,
                           Thr, Tyr, Asp, Glu, Lys, Trp, Asn, Gln, His, Arg)

  object MonoIsotopicMass {
    // tolerate differences in the last decimal place at which monoIsotopicMasses specified
    // i.e., we consider masses upto 0.001 away from each other to be identical
    // note that the mass of an electron is 5.5e-4 Da, so we allow upto around an electron mass
    val defaultNumPlaces = 2
    def tolerance(numDec: Int = defaultNumPlaces): Double = math.pow(10, -numDec)
    def isLt(a: MonoIsotopicMass, b: MonoIsotopicMass) = a.initMass < b.initMass
  }

  class MonoIsotopicMass(val initMass: Double) {

    // `scaled` holds the value of initMass rounded to integers and scaled by 10^defaultNumPlaces
    // Reason we keep the scaling (and not just the truncated double value) is that allows us to
    // get away from floating point rounding errors. With the scaled value, we also get to keep
    // the type as `Long`. With all of that `hashCode` and `equals` are proper and don't introduce
    // errors. We were seeing values such as 5.944444444444445 and 100.07600000000001 in the output
    // when `scaled` was typed as `Double` instead of `Long`. 
    private val scaled = roundedAndScaled()

    def rounded(numDec: Int = MonoIsotopicMass.defaultNumPlaces): Double = roundedAndScaled(numDec) * MonoIsotopicMass.tolerance(numDec)
    def roundedAndScaled(numDec: Int = MonoIsotopicMass.defaultNumPlaces): Long = math round (initMass / MonoIsotopicMass.tolerance(numDec))

    // This function is a helper to `equals`
    // It tests whether two values are within the range of experimental drift we allow
    private def withinDriftWindow(a: Double, b: Double) = (math abs (a - b)) < MonoIsotopicMass.tolerance()

    // we allow for times to drift by driftTolerated, and so equals matches times that only that apart
    override def equals(that: Any) = that match { 
      case that: MonoIsotopicMass => withinDriftWindow(this.initMass, that.initMass)
      case _ => false
    }

    // We deliberately cause hash collisions on *all* MonoIsotopicMass objects. That is because the original
    // code was using a hashcode implementation that was `roundedAndScaled.hashCode`, i.e., converting to
    // rounded integers and taking their hashcode. That causes problems at the bucket boundaries of the 
    // masses. And that cannot be resolved with higher num decimals being hashed, as there will always be
    // bucket boundaries at that precision that will fail.
    // TODO: fix this: We need to go through the code and wherever MonoIsotopicMass objects are compared
    // or put into hashmaps we need to change that into an explicit equality comparison
    override def hashCode() = 1

    override def toString = toString()

    def toString(numDecimals: Int = MonoIsotopicMass.defaultNumPlaces): String = {
      String.format(s"%.${numDecimals}f", this.rounded(numDecimals): java.lang.Double)
    }

    // case when we might want to add: set of atoms together in a formula. need its full mass
    def +(that: MonoIsotopicMass) = new MonoIsotopicMass(this.initMass + that.initMass)
    // case when we might need to sub: when a moeity gets removed from the mol. need remaining mass
    def -(that: MonoIsotopicMass) = new MonoIsotopicMass(this.initMass - that.initMass)
    // case when we might need to multiply by an integer: k molecules together. need combined mass
    def *(num: Int) = new MonoIsotopicMass(this.initMass * num)

    def isIn(low: Double, high: Double): Boolean = rounded() >= low && rounded() <= high
  }

  object Helpers {
    def fromSymbol(sym: Char): AminoAcid = ChemicalSymbols.AllAminoAcids.find(_.symbol.equals(sym)) match {
      case Some(aa) => aa
      case None => throw new Exception("Invalid symbol for an amino acid.")
    }

    val atomOrderInFormula = List(C, H, N, O, S, P)
    def computeFormulaFromElements(elems: Map[Atom, Int]) = {

      // for a pair such as (C, 2) or (N, 5), this fn will convert it to `C2` or `N5`
      def elemnum(atom: Atom, num: Int) = (atom, num) match {
        case (_, 0) => ""
        case (atom, 1) => atom.symbol
        case (atom, num) => atom.symbol + num.toString
      }

      // for each atom in the ordered list (arbitrarily, but consistently ordered) convert it 
      // to tuples `(atom, val of atom in input map OR 0 if not specified)`, and then convert
      // the tuple to string using the flattening function above.
      val orderedAtomAndCounts = atomOrderInFormula.map(a => elemnum(a, (elems.getOrElse(a, 0))))

      // return the concatenated string of all `C2` and `N5` to get the full formula.
      orderedAtomAndCounts.reduce(_ + _)
    }

    def computeMassFromAtomicFormula(elems: Map[Atom, Int]): MonoIsotopicMass = {
      // for each pair such as (C, 2) specified in the elemental composition of an AA, first convert
      // it `massOf(C) * 2` (the `.map` below), and then add them together (the `.reduce` below)
      elems.map{ case (atom, num) => atom.mass * num }.reduce(_ + _)
    }

    def computeChemicalFormulaFromAAFormula(aaForm: Map[AminoAcid, Int]): Map[Atom, Int] = {
      def multiply(es: Map[Atom, Int], times: Int) = es.mapValues(v => v * times)

      def combineElems(e1s: Map[Atom, Int], e2s: Map[Atom, Int]): Map[Atom, Int] = {
        val allAtoms = e1s.keySet ++ e2s.keySet
        val added = allAtoms.map(a => (a, e1s.getOrElse(a, 0) + e2s.getOrElse(a, 0))).toMap
        added
      }

      // for each aminoacid in the aminoacid formula, get its atomic composition (aa.elems)
      // and multiply it by the number of times that amino acid appears, and then combine
      // all the resulting chemical formulae
      aaForm.map{ case (aa, n) => multiply(aa.elems, n) }.reduce(combineElems)
    }
  }
}


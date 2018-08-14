package lin.scheduleGen

object Utils {
  type Pair = (Int, Int)

  def generateTuples(d: Int, ids: List[Int]): List[List[Int]]= {
    generateTuples(d, ids, List(List())).filter(x => x.size == d)
  }

  private def generateTuples(d: Int, ids: List[Int], tuples: List[List[Int]]): List[List[Int]]= {
    def addEvents(ids: List[Int], tuple: List[Int]): List[List[Int]] =
      ids.map(e => if(!tuple.contains(e)) e :: tuple else tuple)

    if(d <= 0) tuples
    else tuples.flatMap(t => generateTuples(d-1, ids, addEvents(ids, t)))
  }

}

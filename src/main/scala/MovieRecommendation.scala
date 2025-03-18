package local.khoa

import org.apache.spark.sql._
import org.apache.spark.sql.functions.{col, desc}
import org.apache.spark.sql.types.{IntegerType, StringType, StructType}

object MovieRecommendation {

  case class MovieNames(movieId: Int, movieTitle: String)
  case class MoviePairSimilarity(
    movie1Id: Int,
    movie1Rating: Long,
    movie2Id: Int,
    movie2Rating: Long,
    similarityRating: Long,
    sumRating: Long,
    JaccardSimilarity: Double)

  def getMovieName(movieNames: Dataset[MovieNames], movieId: Int): String = {
    val result = movieNames.filter(col("movieId") === movieId)
      .select("movieTitle")
      .collect()(0)

    result(0).toString
  }

  def main(args: Array[String]): Unit = {

    if (args.length > 0) {
      val movieId = args(0).toInt

      var similarityThreshold = 0.0
      if (args.length > 1)
        similarityThreshold = args(1).toDouble

      val spark = SparkSession
        .builder()
        .appName("MovieRecommendation")
        .master("spark://spark-master:7077")
        .getOrCreate()

      println("Reading data ...")

      import spark.implicits._
      val moviePairSimilarities = spark
        .read
        .option("header", "true")
        .option("inferSchema", "true")
        .csv("./ItemBasedJaccardSpark/data/output/moviesimilarities_itembased.csv")
        .as[MoviePairSimilarity]

      val movieNamesSchema = new StructType()
        .add("movieId", IntegerType, nullable = true)
        .add("movieTitle", StringType, nullable = true)

      val movieNames = spark
        .read
        .option("sep", "::")
        .option("charset", "ISO-8859-1")
        .schema(movieNamesSchema)
        .csv("./ItemBasedJaccardSpark/data/movies.dat")
        .as[MovieNames]


      println("Filtering ...")
      val filteredResult = moviePairSimilarities
        .filter(($"movie1Id" === movieId || $"movie2Id" === movieId) && $"JaccardSimilarity" >= similarityThreshold)
        .orderBy(desc("JaccardSimilarity"))
        .take(10)

      val inputMovieName = getMovieName(movieNames, movieId)
      println(s"Input movie name: ${inputMovieName}")
      println(s"Input threshold: ${similarityThreshold}")

      println("Similar movies:")
      for (result <- filteredResult) {
        var similarMovieId = result.movie1Id
        if (similarMovieId == movieId)
          similarMovieId = result.movie2Id
        println(getMovieName(movieNames, similarMovieId) + "\tscore: " + result.JaccardSimilarity)
      }

      spark.stop()
    }
  }

}
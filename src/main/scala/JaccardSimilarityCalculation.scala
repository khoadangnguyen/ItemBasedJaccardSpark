package local.khoa

import org.apache.spark.sql._
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._
import java.io.PrintWriter

object JaccardSimilarityCalculation {

  case class MoviesRatings(userId: Int, movieId: Int, rating: Int, timestamp: Long)
  case class MoviePairSimilarity(
    movie1Id: Int,
    movie1Rating: Long,
    movie2Id: Int,
    movie2Rating: Long,
    similarityRating: Long,
    sumRating: Long,
    JaccardSimilarity: Double)

  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName("ItemBasedJaccardSpark")
      .master("spark://spark-master:7077")
      .getOrCreate()

    val moviesRatingsSchema = new StructType()
      .add("userId", IntegerType, nullable = true)
      .add("movieId", IntegerType, nullable = true)
      .add("rating", IntegerType, nullable = true)
      .add("timestamp", LongType, nullable = true)

    var ratingsFilename = "./ItemBasedJaccardSpark/data/ratings.dat"
    if (args.length > 0)
      ratingsFilename = args(0)

    println("Reading ratings data from file ...")
    import spark.implicits._
    val moviesRatings = spark
      .read
      .option("sep", "::")
      .schema(moviesRatingsSchema)
      .csv(ratingsFilename)
      .as[MoviesRatings]

    println("Calculating similarities ...")
    val ratings = moviesRatings.select("movieId", "userId", "rating")

    val ratingsByMovieId = ratings
      .groupBy($"movieId")
      .agg(sum($"rating").alias("totalRating"))

    val moviePairs = ratings.as("ratings1")
      .join(ratings.as("ratings2"),
        $"ratings1.movieId" < $"ratings2.movieId" && $"ratings1.userId" === $"ratings2.userId")
      .select(
        $"ratings1.movieId".alias("movie1Id"),
        $"ratings2.movieId".alias("movie2Id"),
        least($"ratings1.rating", $"ratings2.rating").alias("minRating"))

    val moviePairsSimilarity = moviePairs
      .groupBy($"movie1Id", $"movie2Id")
      .agg(sum($"minRating").alias("similarityRating"))
      .join(ratingsByMovieId.as("movie1Rating"), $"movie1Id" === $"movie1Rating.movieId" )
      .join(ratingsByMovieId.as("movie2Rating"), $"movie2Id" === $"movie2Rating.movieId" )
      .select(
        $"movie1Id",
        $"movie1Rating.totalRating".alias("movie1Rating"),
        $"movie2Id",
        $"movie2Rating.totalRating".alias("movie2Rating"),
        $"similarityRating",
        ($"movie1Rating.totalRating" + $"movie2Rating.totalRating").alias("sumRating"),
        ($"similarityRating"/($"movie1Rating.totalRating" + $"movie2Rating.totalRating")).alias("JaccardSimilarity"))
      .as[MoviePairSimilarity]

    val moviePairsSimilaritySorted = moviePairsSimilarity
      .orderBy(
        desc("similarityRating"),
        desc("JaccardSimilarity"),
        desc("sumRating"),
        $"movie1Id", $"movie2Id")
      .collect()
    spark.stop()

    println("Writing similarities data to file ...")
    var outputFilename = "./ItemBasedJaccardSpark/data/output/moviesimilarities_itembased.csv"
    if (args.length > 2)
      outputFilename = args(0)
    val writer = new PrintWriter(outputFilename)
    writer.println("movie1Id,movie1Rating,movie2Id,movie2Rating,similarityRating,sumRating,JaccardSimilarity")
    moviePairsSimilaritySorted.foreach(sim
      => writer.println(s"" +
        s"${sim.movie1Id}," +
        s"${sim.movie1Rating}," +
        s"${sim.movie2Id}," +
        s"${sim.movie2Rating}," +
        s"${sim.similarityRating}," +
        s"${sim.sumRating}," +
        s"${sim.JaccardSimilarity}"
      ))
    writer.close()
    println("Finished")
  }

}
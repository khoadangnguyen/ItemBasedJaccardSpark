name := "ItemBasedJaccardSpark"
version := "0.1"
scalaVersion := "2.12.20"

// Add Spark Dependencies
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "3.5.4",
  "org.apache.spark" %% "spark-sql"  % "3.5.4"
)

lazy val packageJaccardSimilarityCalculation = taskKey[Unit]("Package JaccardSimilarityCalculation into a JAR")
lazy val packageMovieRecommendation = taskKey[Unit]("Package MovieRecommendation into a JAR")

packageJaccardSimilarityCalculation := {
  val jarFile = (Compile / packageBin).value
  val dest = file(s"target/JaccardSimilarityCalculation.jar")
  IO.copyFile(jarFile, dest)
  println(s"JaccardSimilarityCalculation JAR built: $dest")
}

packageMovieRecommendation := {
  val jarFile = (Compile / packageBin).value
  val dest = file(s"target/MovieRecommendation.jar")
  IO.copyFile(jarFile, dest)
  println(s"MovieRecommendation JAR built: $dest")
}

// Specify separate main classes dynamically
Compile / packageBin / mainClass := {
  if (sys.props.getOrElse("mainClass", "") == "local.khoa.JaccardSimilarityCalculation") Some("JaccardSimilarityCalculation")
  else if (sys.props.getOrElse("mainClass", "") == "local.khoa.MovieRecommendation") Some("MovieRecommendation")
  else None
}
//Compile / packageBin / mainClass := Some("local.khoa.JaccardSimilarityCalculation")
//Compile / packageBin / artifactPath := file("target/ItemBasedJaccardSpark.jar")

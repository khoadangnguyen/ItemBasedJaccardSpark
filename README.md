# ItemBasedJaccardSpark
ItemBasedJaccardSpark is a Spark-Scala project that finds similar movies using the item-based Jaccard similarity approach

## Table of Contents
1. [Overview](#1-overview)
2. [Data](#2-data)
3. [Setup](#3-setup)
4. [Item-Based Jaccard Similarity and Implementation](#4-implementation)
5. [Performance](#5-performance)
6. [Extensions and Future Work](#6-extension)

## 1. Overview
This project calculates movie similarity based on user ratings using the **item-based Jaccard similarity** approach. The 
goal is to compute similarities between movies by comparing the set of users who have rated those movies, which can then
be used to build a recommendation system.

This project consists of 2 main parts:

1. Calculating Movie Similarities: In the first part, movie ratings are read, and the similarities between movie pairs 
(identified by movieIds) are calculated based on the Jaccard similarity index.

2. Movie Recommendation: In the second part, a movieId is provided as input, and the system searches for and recommends 
similar movies based on the pre-calculated similarities. The movie name is retrieved and displayed for better clarity.


## 2. Data
The movie data (titles and ratings) used in this project is sourced from the [MovieLens dataset](https://grouplens.org/datasets/movielens/), 
which contains 1 million ratings provided by 6,000 users across 4,000 movies.


## 3. Setup
1. Docker is used to set up a standalone Spark cluster with bitnami/spark:3.5.4 image.
2. Spark (3.5.4) applications are implemented in Scala (2.12.20).
3. sbt (1.10.3) is used to package Scala applications into JAR files, which are then submitted to the Spark cluster for execution


## 4. Item-Based Jaccard Similarity and Implementation

### 4.1 Item-Based Jaccard Similarity
Item-based Jaccard Similarity is a method used to measure the similarity between two movies based on the users who have rated them.
It is particularly useful in recommendation systems to find similar items.

The Jaccard Similarity between two movie is calculated as the ratio of the number of users who rated both movies to the 
total number of users who have rated at least one of the items. It is defined as:

$$
J(A, B) = \frac{|A \cap B|}{|A \cup B|}
$$

Where:
- A and B are sets of users who have rated movies i and j.
- |A ∩ B| represents the number of users who rated both movies.
- |A ∪ B| represents the number of users who rated at least one of the two movies.

This formula helps me calculate the similarity between two movies based on the number of common users who have rated them.

### 4.2 Implementation


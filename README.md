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
3. sbt (1.10.3) is used to package Scala applications into JAR files, which are then submitted to the Spark cluster for execution.


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

### 4.2 Applying Item-Based Jaccard Similarity
Since ratings are 1-to-5-star scale, the implementation applies Jaccard similarity using bag-based approach.

#### 4.2.1 Jaccard similarity for bags
When a user u rates a movie m with n stars, the movie bag of m contains n copies of user u. 

Example
Ratings for m1
* User u1 rates movie m1 5 stars
* User u2 rates movie m1 4 stars
* User u3 rates movie m1 3 stars 

The user bag for movie m1:
{u1, u1, u1, u1, u1, u2, u2, u2, u2, u3, u3, u3}

Ratings for m2
* User u1 rates movie m2 3 stars
* User u2 rates movie m2 2 stars
* User u4 rates movie m2 4 stars

The user bag for movie m2:
{u1, u1, u1, u2, u2, u4, u4, u4, u4}

Calculating Jaccard Similarity

|m1 ∩ m2| = |{u1, u1, u1, u1, u1, u2, u2, u2, u2, u3, u3, u3} ∩ {u1, u1, u1, u2, u2, u4, u4, u4, u4}| 
= |{u1, u1, u1, u2, u2}| = 5

|m1 ∪ m2| = |{u1, u1, u1, u1, u1, u2, u2, u2, u2, u3, u3, u3} ∪ {u1, u1, u1, u2, u2, u4, u4, u4, u4}|
= |{u1, u1, u1, u1, u1, u2, u2, u2, u2, u3, u3, u3, u1, u1, u1, u2, u2, u4, u4, u4, u4}| = 21

J(m1, m2) = 5/21 = 0.23809524

#### 4.2.2 Item-based Jaccard Similarity Calculation Implementation

The input ratings data itself resembles a bag, where each row contains movieId, userId, and rating. The rating value 
represents the number of times that userId appears in the movie’s bag.

To determine the total occurrences of all users in a movie’s bag, the data is grouped by movieId, and the sum of ratings 
is calculated.

Next, the ratings data is self-joined to create movie pairs, ensuring that only movies rated by the same users are considered. 
The number of common users between two movie bags is determined by taking the minimum rating count between the two movies 
for each shared user.

Finally, the Jaccard Similarity is computed as:

$$
J(m_1, m_2) = \frac{\text{Number of common users}}{\text{Sum of ratings for both movies in the pair}}
$$

#### 4.2.3 Movie Recommendation Implementation

Given an input movieId, similar movies are identified by filtering precomputed Jaccard similarity scores for movie pairs.

Additionally, movie names are read from the input dataset and retrieved for display, ensuring that the recommendations 
are presented with their corresponding titles.

## 5. Performance
Using a dataset of 1 million ratings from 6,000 users across 4,000 movies, the Jaccard Similarity computation completes 
in approximately 1 minute.

The execution runs on a Spark cluster with a total of 4 cores allocated across all executors, where each executor is 
assigned 512MB of memory.

## 6. Extension and Future Work
* Implement a metric to compare the effectiveness of different recommendation algorithms.
* Develop a customer-based approach for similarity computation.
* Redesign the system for a streaming architecture to handle real-time data.
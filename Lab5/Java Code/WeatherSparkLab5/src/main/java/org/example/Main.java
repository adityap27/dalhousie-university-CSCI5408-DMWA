package org.example;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;


public class Main {
    public static void main(String[] args) {

        SparkConf sparkConf = new SparkConf().setAppName("Weather Data Filter").setMaster("local[*]");
        JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);

        SparkSession spark = SparkSession
                .builder()
                .appName("Weather Data Filter")
                .getOrCreate();

        // File to DataFrame
        Dataset<Row> weatherData = spark.read().option("multiline","true").json("file:///home/adityapurohit27/weather.json");

        // Filter the daily array as per the feels_like.daily value.
        Dataset<Row> filteredWeatherData = weatherData.withColumn("daily", functions.expr("filter(daily, x -> x.feels_like.day < 15.0)"));

        // Show the filtered data
        filteredWeatherData.show();

        // Store the file
        filteredWeatherData.write().format("json").save("file:///home/adityapurohit27/fall_weather");

        // Stop the spark session and context
        spark.stop();
        sparkContext.stop();
    }
}
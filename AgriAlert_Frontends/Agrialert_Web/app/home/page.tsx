"use client"
import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { fetchWeatherApi } from 'openmeteo';
import { Card, CardHeader, CardTitle, CardContent, CardDescription } from '@/components/ui/card';
import { Carousel, CarouselContent, CarouselItem, CarouselNext, CarouselPrevious } from '@/components/ui/carousel';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Badge } from '@/components/ui/badge';
import { Cloud, Droplets, Thermometer, Wind, ChevronRight, AlertTriangle, LineChart, Lightbulb } from 'lucide-react';
import { useUser } from '../context/UserContext';
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { ScrollArea } from "@/components/ui/scroll-area";


interface WeatherData {
  temperature: number;
  windspeed: number;
  humidity: number;
  precipitation: number;
}

interface NewsArticle {
  id: number;
  title: string;
  text: string;
  url: string;
  image: string;
  publish_date: string;
}

interface CropAlert {
  type: string;
  title: string;
  message: string;
  severity: string;
}

interface CropAnalysis {
  overallSeverity: string;
  alerts: CropAlert[];
  recommendations: { message: string }[];
  insights: string[];
}

interface CropData {
  cropAnalyses: {
    [key: string]: CropAnalysis;
  };
  errors: string[];
}

const HomePage = () => {
  const [weather, setWeather] = useState<WeatherData | null>(null);
  const [newsArticles, setNewsArticles] = useState<NewsArticle[]>([]);
  const [cropData, setCropData] = useState<CropData | null>(null);
  const [location, setLocation] = useState({ latitude: 0, longitude: 0 });
  const [forecastData, setForecastData] = useState<{
    maxTemp: number;
    minTemp: number;
    maxRain: number;
    minRain: number;
  } | null>(null);

  const { user, setUser } = useUser();

  useEffect(() => {
    navigator.geolocation.getCurrentPosition((position) => {
      setLocation({
        latitude: position.coords.latitude,
        longitude: position.coords.longitude
      });
    });
  }, []);

  useEffect(() => {
    if (location.latitude && location.longitude) {
      const fetchForecastData = async () => {
        try {
          const params = {
            latitude: location.latitude,
            longitude: location.longitude,
            hourly: ["temperature_2m", "rain"],
            daily: ["temperature_2m_max", "temperature_2m_min", "rain_sum"],
            forecast_days: 2
          };

          const url = "https://api.open-meteo.com/v1/forecast";
          const responses = await fetchWeatherApi(url, params);
          const response = responses[0];

          const utcOffsetSeconds = response.utcOffsetSeconds();
          const hourly = response.hourly()!;
          const daily = response.daily()!;

          // Helper function for time range
          const range = (start: number, stop: number, step: number) =>
            Array.from({ length: (stop - start) / step }, (_, i) => start + i * step);

          // Get hourly data
          const hourlyData = {
            time: range(Number(hourly.time()), Number(hourly.timeEnd()), hourly.interval()).map(
              (t) => new Date((t + utcOffsetSeconds) * 1000)
            ),
            temperature2m: hourly.variables(0)!.valuesArray()!,
            rain: hourly.variables(1)!.valuesArray()!,
          };

          // Get daily data
          const dailyData = {
            time: range(Number(daily.time()), Number(daily.timeEnd()), daily.interval()).map(
              (t) => new Date((t + utcOffsetSeconds) * 1000)
            ),
            temperature2mMax: daily.variables(0)!.valuesArray()!,
            temperature2mMin: daily.variables(1)!.valuesArray()!,
          };

          const tomorrowMaxTemp = dailyData.temperature2mMax[1];
          const tomorrowMinTemp = dailyData.temperature2mMin[1];

          // Get rain data for tomorrow (hours 24-47 represent the second day)
          const tomorrowRainData = hourlyData.rain.slice(24, 48);
          // Convert Float32Array to regular array before using spread operator
          const rainArray = Array.from(tomorrowRainData);
          const maxRain = Math.max(...rainArray);
          const minRain = Math.min(...rainArray);

          setForecastData({
            maxTemp: tomorrowMaxTemp,
            minTemp: tomorrowMinTemp,
            maxRain,
            minRain
          });

          // Fetch current weather data
          const currentWeatherResponse = await axios.get(
            `https://api.open-meteo.com/v1/forecast?latitude=${location.latitude}&longitude=${location.longitude}&current=temperature_2m,windspeed_10m,relative_humidity_2m,precipitation`
          );

          setWeather({
            temperature: currentWeatherResponse.data.current.temperature_2m,
            windspeed: currentWeatherResponse.data.current.windspeed_10m,
            humidity: currentWeatherResponse.data.current.relative_humidity_2m,
            precipitation: currentWeatherResponse.data.current.precipitation
          });
        } catch (error) {
          console.error('Error fetching forecast data:', error);
        }
      };


      // Fetch news
      const fetchNews = async () => {
        try {
          const response = await axios.get('https://api.worldnewsapi.com/search-news', {
            params: {
              text: 'agriculture',
              'source-country': 'ma',
              number: 5,
              'api-key': '9f7f2f2a7052471f9fc6d6e478b38a77'
            }
          });
          setNewsArticles(response.data.news);
        } catch (error) {
          console.error('Error fetching news:', error);
        }
      };

      fetchForecastData();
      fetchNews();
    }
  }, [location]);

  // Fetch crop analysis with forecast data
  const fetchCropData = async () => {
    try {
      if (!forecastData) return;

      const token = localStorage.getItem('JWTtoken');
      console.log(user?.crops)
      console.log(forecastData.maxTemp, forecastData.minTemp, forecastData.maxRain, forecastData.minRain);
      const requestBody = {
        maxTemp: forecastData.maxTemp,
        minTemp: forecastData.minTemp,
        maxRain: forecastData.maxRain,
        minRain: forecastData.minRain,
        cropNames: user?.crops,
      };

      const response = await axios.post('http://localhost:8087/api/crops/weather-analysis',
        requestBody,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      setCropData(response.data);
    } catch (error) {
      console.error('Error fetching crop data:', error);
    }
  };
  // Trigger crop data fetch when forecast data is available
  useEffect(() => {
    if (forecastData) {
      fetchCropData();
    }
  }, [forecastData]);

  const getSeverityColor = (severity: string) => {
    switch (severity.toUpperCase()) {
      case 'HIGH': return 'bg-red-100 text-red-800';
      case 'MEDIUM': return 'bg-yellow-100 text-yellow-800';
      case 'LOW': return 'bg-green-100 text-green-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  return (
    <div className="p-6 space-y-6">
      {/* Weather Section */}
      <Card className="shadow-lg rounded-lg border-0 bg-gradient-to-r from-blue-200 via-indigo-200 to-purple-200">
        <CardHeader>
          <CardTitle className="text-xl font-semibold">Current Weather</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-6">
            <div className="flex items-center space-x-4">
              <Thermometer className="w-8 h-8 text-blue-600" />
              <div>
                <p className="text-sm text-gray-500">Temperature</p>
                <p className="text-2xl font-bold">{weather?.temperature}°C</p>
              </div>
            </div>
            <div className="flex items-center space-x-4">
              <Wind className="w-8 h-8 text-green-600" />
              <div>
                <p className="text-sm text-gray-500">Wind Speed</p>
                <p className="text-2xl font-bold">{weather?.windspeed} km/h</p>
              </div>
            </div>
            <div className="flex items-center space-x-4">
              <Droplets className="w-8 h-8 text-blue-500" />
              <div>
                <p className="text-sm text-gray-500">Humidity</p>
                <p className="text-2xl font-bold">{weather?.humidity}%</p>
              </div>
            </div>
            <div className="flex items-center space-x-4">
              <Cloud className="w-8 h-8 text-gray-600" />
              <div>
                <p className="text-sm text-gray-500">Precipitation</p>
                <p className="text-2xl font-bold">{weather?.precipitation} mm</p>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* News Carousel with Images */}
      <Card>
        <CardHeader>
          <CardTitle>Agricultural News</CardTitle>
        </CardHeader>
        <CardContent>
          <Carousel className="w-full">
            <CarouselContent>
              {newsArticles.map((article) => (
                <CarouselItem key={article.id} className="md:basis-1/2 lg:basis-1/3">
                  <Card>
                    <div className="relative w-full h-48 overflow-hidden rounded-lg">
                      <img
                        src={article.image}
                        alt={article.title}
                        className="object-cover w-full h-full"
                      />
                    </div>
                    <CardHeader>
                      <CardTitle className="text-lg mt-2">{article.title}</CardTitle>
                    </CardHeader>
                    <CardContent>
                      <CardDescription>{article.text.slice(0, 100)}...</CardDescription>
                      <a href={article.url} className="text-blue-500 hover:underline mt-2 block">
                        Read more
                      </a>
                    </CardContent>
                  </Card>
                </CarouselItem>
              ))}
            </CarouselContent>
            <CarouselPrevious />
            <CarouselNext />
          </Carousel>
        </CardContent>
      </Card>


      {/* Crop Analysis Section cropAnalyses */}
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <h2 className="text-2xl font-semibold">Crop Analysis</h2>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {cropData && Object.entries(cropData.cropAnalyses).map(([cropName, analysis]) => (
            <Card key={cropName} className="overflow-hidden border-none shadow-lg">
              <CardHeader className="bg-gradient-to-r from-slate-50 to-slate-100 dark:from-slate-900 dark:to-slate-800">
                <div className="flex justify-between items-center">
                  <div className="space-y-1">
                    <CardTitle className="text-xl">{cropName}</CardTitle>
                    <CardDescription>Overall Status Analysis</CardDescription>
                  </div>
                  <Badge
                    variant="outline"
                    className={`${getSeverityColor(analysis.overallSeverity)} px-4 py-1`}
                  >
                    {analysis.overallSeverity}
                  </Badge>
                </div>
              </CardHeader>
              <CardContent className="p-0">
                <Tabs defaultValue="alerts" className="w-full">
                  <TabsList className="w-full justify-start p-0 bg-transparent border-b rounded-none h-12">
                    <TabsTrigger value="alerts" className="flex items-center gap-2 data-[state=active]:border-b-2 rounded-none">
                      <AlertTriangle className="w-4 h-4" />
                      Alerts
                    </TabsTrigger>
                    <TabsTrigger value="recommendations" className="flex items-center gap-2 data-[state=active]:border-b-2 rounded-none">
                      <LineChart className="w-4 h-4" />
                      Actions
                    </TabsTrigger>
                    <TabsTrigger value="insights" className="flex items-center gap-2 data-[state=active]:border-b-2 rounded-none">
                      <Lightbulb className="w-4 h-4" />
                      Insights
                    </TabsTrigger>
                  </TabsList>

                  <ScrollArea className="h-[300px] w-full">
                    <TabsContent value="alerts" className="p-4 m-0">
                      {analysis.alerts.length > 0 ? (
                        <div className="space-y-3">
                          {analysis.alerts.map((alert, index) => (
                            <Alert
                              key={index}
                              className={`${getSeverityColor(alert.severity)} border-l-4 transition-all hover:translate-x-1`}
                            >
                              <AlertTitle className="flex items-center gap-2">
                                <AlertTriangle className="w-4 h-4" />
                                {alert.title}
                              </AlertTitle>
                              <AlertDescription>{alert.message}</AlertDescription>
                            </Alert>
                          ))}
                        </div>
                      ) : (
                        <div className="flex flex-col items-center justify-center h-48 text-gray-500">
                          <div className="bg-green-100 rounded-full p-3 mb-4">
                            <AlertTriangle className="w-6 h-6 text-green-600" />
                          </div>
                          <p className="text-lg font-medium">No Active Alerts</p>
                          <p className="text-sm text-center mt-2">Your {cropName} is doing well with no issues to report.</p>
                        </div>
                      )}
                    </TabsContent>

                    <TabsContent value="recommendations" className="p-4 m-0">
                      {analysis.recommendations.length > 0 ? (
                        <div className="space-y-3">
                          {analysis.recommendations.map((rec, index) => (
                            <div key={index} className="flex items-start gap-3 p-3 rounded-lg bg-slate-50 dark:bg-slate-900 transition-all hover:translate-x-1">
                              <ChevronRight className="w-5 h-5 mt-0.5 text-slate-400" />
                              <p className="text-sm">{rec.message}</p>
                            </div>
                          ))}
                        </div>
                      ) : (
                        <div className="flex flex-col items-center justify-center h-48 text-gray-500">
                          <div className="bg-green-100 rounded-full p-3 mb-4">
                            <LineChart className="w-6 h-6 text-green-600" />
                          </div>
                          <p className="text-lg font-medium">No Actions Required</p>
                          <p className="text-sm text-center mt-2">Current conditions are optimal for your {cropName}.</p>
                        </div>
                      )}
                    </TabsContent>

                    <TabsContent value="insights" className="p-4 m-0">
                      {analysis.insights.length > 0 ? (
                        <div className="space-y-3">
                          {analysis.insights.map((insight, index) => (
                            <div key={index} className="flex items-start gap-3 p-3 rounded-lg bg-slate-50 dark:bg-slate-900 transition-all hover:translate-x-1">
                              <Lightbulb className="w-5 h-5 mt-0.5 text-slate-400" />
                              <p className="text-sm">{insight}</p>
                            </div>
                          ))}
                        </div>
                      ) : (
                        <div className="flex flex-col items-center justify-center h-48 text-gray-500">
                          <div className="bg-green-100 rounded-full p-3 mb-4">
                            <Lightbulb className="w-6 h-6 text-green-600" />
                          </div>
                          <p className="text-lg font-medium">No New Insights</p>
                          <p className="text-sm text-center mt-2">Everything is progressing as expected for your {cropName}.</p>
                        </div>
                      )}
                    </TabsContent>
                  </ScrollArea>
                </Tabs>
              </CardContent>
            </Card>
          ))}
        </div>
      </div>
    </div>
  );
};

export default HomePage;


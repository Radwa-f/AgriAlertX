import React from "react";
import { Section, Container } from "@/components/craft";
import Balancer from "react-wrap-balancer";
import { AlertTriangle, CloudRain, Thermometer } from "lucide-react";

type FeatureText = {
  icon: JSX.Element;
  title: string;
  description: string;
  gradient: string;
  shadowColor: string;
};

const featureText: FeatureText[] = [
  {
    icon: <AlertTriangle className="h-7 w-7" />,
    title: "Système d'Alerte Précoce",
    description:
      "Recevez des notifications instantanées sur les risques climatiques potentiels menaçant vos cultures, permettant une intervention rapide.",
    gradient: "from-red-500/20 via-red-500/10 to-transparent",
    shadowColor: "group-hover:shadow-red-500/20",
  },
  {
    icon: <CloudRain className="h-7 w-7" />,
    title: "Analyse des Données Météorologiques",
    description:
      "Utilisez des données climatiques locales précises pour anticiper et minimiser les impacts des catastrophes naturelles sur l'agriculture.",
    gradient: "from-blue-500/20 via-blue-500/10 to-transparent",
    shadowColor: "group-hover:shadow-blue-500/20",
  },
  {
    icon: <Thermometer className="h-7 w-7" />,
    title: "Recommandations Adaptatives",
    description:
      "Recevez des conseils personnalisés pour renforcer la résilience de vos cultures face aux changements climatiques et aux risques environnementaux.",
    gradient: "from-yellow-500/20 via-yellow-500/10 to-transparent",
    shadowColor: "group-hover:shadow-yellow-500/20",
  },
];

const Services = () => {
  return (
    <Section className="relative overflow-hidden p-10 m-8">
      {/* Background decoration */}
      <div className="absolute inset-0 bg-gradient-to-b from-transparent via-green-50/30 to-transparent dark:via-green-950/20" />
      <div className="absolute -left-40 top-40 w-96 h-96 bg-green-100/40 dark:bg-green-900/20 rounded-full blur-3xl" />
      
      <Container className="not-prose relative">
        <div className="flex flex-col gap-6">
          {/* Title badge */}
          <div className="inline-flex items-center w-fit px-3 py-1 rounded-full text-sm font-medium bg-green-50 dark:bg-green-900/50 text-green-800 dark:text-green-200 border border-green-100 dark:border-green-800 mb-4">
            <span className="w-1.5 h-1.5 rounded-full bg-green-500 mr-2 animate-pulse" />
            Nos Services
          </div>
          
          <h3 className="text-4xl h-[70px] font-bold bg-gradient-to-r from-green-700 via-green-600 to-green-500 dark:from-green-400 dark:via-green-300 dark:to-green-200 bg-clip-text text-transparent">
            <Balancer>
              Protégez vos cultures, anticipez les risques
            </Balancer>
          </h3>
          
          <h4 className="text-xl text-gray-600 dark:text-gray-300">
            <Balancer>
              Une solution technologique pour la sécurité alimentaire et la résilience agricole
            </Balancer>
          </h4>

          <div className="mt-8 grid gap-6 md:grid-cols-3">
            {featureText.map(({ icon, title, description, gradient, shadowColor }, index) => (
              <div
                className={`group flex flex-col gap-4 p-6 rounded-xl 
                  bg-white dark:bg-black border border-gray-200 dark:border-gray-800
                  hover:border-transparent dark:hover:border-transparent
                  transition-all duration-500 relative overflow-hidden
                  ${shadowColor} hover:shadow-2xl`}
                key={index}
              >
                {/* Gradient background */}
                <div className={`absolute top-0 left-0 w-full h-full bg-gradient-to-br ${gradient} opacity-0 group-hover:opacity-100 transition-opacity duration-500`} />
                
                {/* Icon container */}
                <div className={`relative z-10 w-12 h-12 rounded-lg flex items-center justify-center
                  ${index === 0 ? 'bg-red-100 text-red-500 dark:bg-red-950/50' : 
                    index === 1 ? 'bg-blue-100 text-blue-500 dark:bg-blue-950/50' : 
                    'bg-yellow-100 text-yellow-500 dark:bg-yellow-950/50'}
                  transform group-hover:scale-110 transition-transform duration-300`}>
                  {icon}
                </div>
                
                {/* Content */}
                <div className="relative z-10 space-y-2">
                  <h4 className={`text-xl font-semibold
                    ${index === 0 ? 'text-red-500 dark:text-red-400' : 
                      index === 1 ? 'text-blue-500 dark:text-blue-400' : 
                      'text-yellow-500 dark:text-yellow-400'}`}>
                    {title}
                  </h4>
                  <p className="text-gray-600 dark:text-gray-300 leading-relaxed">
                    {description}
                  </p>
                </div>
                
                {/* Decorative arrow */}
                <div className={`absolute bottom-4 right-4 w-6 h-6 rounded-full
                  ${index === 0 ? 'bg-red-500/10' : 
                    index === 1 ? 'bg-blue-500/10' : 
                    'bg-yellow-500/10'}
                  flex items-center justify-center opacity-0 group-hover:opacity-100
                  transform translate-x-4 group-hover:translate-x-0
                  transition-all duration-300`}>
                  <span className="text-lg">→</span>
                </div>
              </div>
            ))}
          </div>
        </div>
      </Container>
    </Section>
  );
};

export default Services;
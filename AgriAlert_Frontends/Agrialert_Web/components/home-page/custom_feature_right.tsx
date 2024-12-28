import React from 'react';
import Link from "next/link";
import Image from "next/image";
import { Section, Container } from "@/components/craft";
import { Button } from "@/components/ui/button";
import { CloudRain, ArrowRight, ChevronRight } from "lucide-react";
import farmerImage from "@/public/farmer.jpg"; // Updated image name

const ModernFeatureRight = () => {
  return (
    <Section className="relative overflow-hidden p-10 m-8">
      {/* Decorative background elements */}
      <div className="absolute inset-0 bg-gradient-to-b from-transparent via-green-50/30 to-transparent dark:via-green-950/20" />
      <div className="absolute -right-40 top-40 w-96 h-96 bg-green-100/40 dark:bg-green-900/20 rounded-full blur-3xl" />
      
      <Container className="relative grid items-stretch md:grid-cols-2 md:gap-16 py-24">
        <div className="flex flex-col gap-8 py-8 md:py-12">
          {/* Badge */}
          <div className="inline-flex items-center w-fit px-4 py-2 rounded-full text-sm font-medium bg-green-50 dark:bg-green-900/50 text-green-800 dark:text-green-200 border border-green-100 dark:border-green-800">
            <span className="w-2 h-2 rounded-full bg-green-500 mr-2 animate-pulse" />
            Analyse en Temps Réel
          </div>

          {/* Heading with gradient */}
          <h3 className="!my-0 text-4xl font-bold bg-gradient-to-r from-green-700 via-green-600 to-green-500 dark:from-green-400 dark:via-green-300 dark:to-green-200 bg-clip-text text-transparent">
            Analyse Prédictive des Risques Agricoles
          </h3>

          {/* Description with better typography */}
          <p className="text-lg leading-relaxed text-gray-600 dark:text-gray-300">
            Notre plateforme Agri-Alert analyse les données météorologiques prévus pour fournir des 
            insights précis sur les risques potentiels affectant vos cultures. 
            <span className="block mt-4">
              Anticipez les catastrophes naturelles, optimisez vos stratégies 
              agricoles et renforcez la résilience de votre exploitation.
            </span>
          </p>

          {/* Feature list */}
          <div className="grid gap-4 mt-2">
            {['Prévisions précises', 'Alertes en temps réel'].map((feature) => (
              <div key={feature} className="flex items-center gap-2 text-gray-600 dark:text-gray-300">
                <ChevronRight className="h-5 w-5 text-green-500" />
                <span>{feature}</span>
              </div>
            ))}
          </div>

          {/* CTA buttons with modern styling */}
          <div className="flex items-center gap-4 mt-4">
            <Button 
              size="lg"
              className="bg-gradient-to-r from-green-600 to-green-500 hover:from-green-500 hover:to-green-400 transform hover:scale-105 transition-all"
              asChild
            >
              <Link href="/alertes">
                <CloudRain className="mr-2 h-5 w-5" />
                Démarrer l'Analyse
              </Link>
            </Button>
            <Button 
              size="lg"
              variant="ghost"
              className="group"
              asChild
            >
              <Link href="/solutions">
                En savoir plus
                <ArrowRight className="ml-2 h-4 w-4 transform group-hover:translate-x-1 transition-transform" />
              </Link>
            </Button>
          </div>
        </div>

        {/* Image container with modern styling */}
        <div className="relative h-[500px] md:h-auto mt-8 md:mt-0">
          <div className="absolute inset-0 bg-gradient-to-t from-green-900/20 to-transparent rounded-2xl z-10" />
          <div className="relative h-full overflow-hidden rounded-2xl border border-white/10 shadow-2xl transform md:hover:scale-[1.02] transition-all duration-700">
            <Image
              src="/farmer.png"
              alt="Surveillance des cultures et analyse des risques"
              className="object-cover object-center rounded-xl"
              fill
              priority
              sizes="(max-width: 768px) 100vw, 50vw"
            />
          </div>
          
          {/* Floating stats card */}
          <div className="absolute bottom-6 right-6 z-20 bg-white/90 dark:bg-black/90 backdrop-blur-lg rounded-xl p-4 shadow-lg border border-white/20 transform hover:-translate-y-1 transition-transform">
            <div className="flex items-center gap-4">
              <div className="h-12 w-12 rounded-full bg-green-100 dark:bg-green-900/50 flex items-center justify-center">
                <CloudRain className="h-6 w-6 text-green-600 dark:text-green-400" />
              </div>
              <div>
                <p className="text-sm font-medium text-gray-600 dark:text-gray-300">Précision des prévisions</p>
                <p className="text-2xl font-bold text-green-600 dark:text-green-400">90%</p>
              </div>
            </div>
          </div>
        </div>
      </Container>
    </Section>
  );
};

export default ModernFeatureRight;
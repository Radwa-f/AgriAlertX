import React from 'react';
import { Card, CardHeader, CardTitle, CardContent, CardDescription, CardFooter } from '@/components/ui/card';
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from '@/components/ui/accordion';
import { Button } from "@/components/ui/button";

const HelpPage = () => {
  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold text-center mb-6">Help Center</h1>

      {/* FAQ Section */}
      <section className="mb-12">
        <h2 className="text-2xl font-semibold mb-4">Frequently Asked Questions (FAQ)</h2>
        <Accordion type="multiple">
          <AccordionItem value={''}>
            <AccordionTrigger>
              <span className="text-lg font-medium">How do I reset my password?</span>
            </AccordionTrigger>
            <AccordionContent>
              <p className="text-gray-700">To reset your password, click on the "Forgot Password" link on the login page, and follow the instructions sent to your email.</p>
            </AccordionContent>
          </AccordionItem>

          <AccordionItem value={''}>
            <AccordionTrigger>
              <span className="text-lg font-medium">How can I contact support?</span>
            </AccordionTrigger>
            <AccordionContent>
              <p className="text-gray-700">You can reach our support team via the "Contact Us" page, or by emailing support@example.com.</p>
            </AccordionContent>
          </AccordionItem>

          <AccordionItem value={''}>
            <AccordionTrigger>
              <span className="text-lg font-medium">How do I update my profile information?</span>
            </AccordionTrigger>
            <AccordionContent>
              <p className="text-gray-700">You can update your profile information from the "Profile" section in your account settings.</p>
            </AccordionContent>
          </AccordionItem>
        </Accordion>
      </section>

      {/* Team Information Section */}
      <section className="mb-12">
        <h2 className="text-2xl font-semibold mb-4">Meet the Team</h2>
        
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-2 gap-8">
        <Card className="shadow-lg">
            <CardHeader className="bg-pink-500 text-white text-center py-4">
              <h3 className="text-xl font-semibold">Radwa Fattouhi</h3>
              <p>Lead Developer</p>
            </CardHeader>
            <CardContent className="px-4 py-6">
              <p className="text-gray-700 text-md">Radwa is an engineering student at the National School of Applied Sciences of El Jadida. As a lead developer at Agrialert, she plays a pivotal role in shaping the software's functionality and ensuring its efficiency.</p>
            </CardContent>
            <CardFooter className="text-center py-2">
              <Button className="bg-pink-500 text-white">Contact</Button>
            </CardFooter>
          </Card>
          <Card className="shadow-lg">
            <CardHeader className="bg-blue-500 text-white text-center py-4">
              <h3 className="text-xl font-semibold">Saifeddine Douidy</h3>
              <p>Lead Developer</p>
            </CardHeader>
            <CardContent className="px-4 py-6">
              <p className="text-gray-700">Saifeddine is an engineering student at the National School of Applied Sciences of El Jadida. As a lead developer, he contributes to the software development for Agrialert, focusing on technical architecture and coding.</p>
            </CardContent>
            <CardFooter className="text-center py-2">
              <Button className="bg-blue-500 text-white">Contact</Button>
            </CardFooter>
          </Card>

          
        </div>
      </section>

      {/* Contact Information Section */}
      <section className="mb-12">
        <h2 className="text-2xl font-semibold mb-4">Contact Information</h2>
        <div className="bg-gray-100 p-6 rounded-lg">
          <h3 className="text-xl font-medium mb-2">Customer Support</h3>
          <p>If you have any questions or need assistance, feel free to contact our customer support team:</p>
          <p className="text-blue-500">Email: <a href="mailto:fattouhiradwa@gmail.com">fattouhiradwa@gmail.com</a> <a href="mailto:douidysifeddine@gmail.com">douidysifeddine@gmail.com</a></p>
          <p className="text-blue-500">Phone: +212 767252054</p>
        </div>
      </section>

      {/* Footer Section */}
      <footer className="text-center text-gray-500 py-4">
        <p>&copy; {new Date().getFullYear()} Agrialert. All rights reserved.</p>
      </footer>
    </div>
  );
};

export default HelpPage;

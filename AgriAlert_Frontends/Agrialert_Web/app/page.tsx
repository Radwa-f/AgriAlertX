import { Main, Section, Container, Box } from "@/components/craft";
import Footer from "@/components/footer";
import CustomFeatureLeft from "@/components/home-page/custom_feature_left";
import CustomFeatureRight from "@/components/home-page/custom_feature_right";
import FeatureLeft from "@/components/home-page/feature-left";
import FeatureRight from "@/components/home-page/feature-right";
import Hero from "@/components/home-page/hero";
import Services from "@/components/home-page/services";
import { NavBar } from "@/components/navbar";

export default async function Home() {
  // Remove the session check and redirection logic
  /* 
  const session = await getServerSession(authOptions);
  if (!session) {
    return redirect('/login');
  } else {
    return redirect('/home');
  }
  */

  // Return your landing page content directly
  return (
    <Main>
      <Section>
      <NavBar/>
        <Container>
          <Hero/>
          <CustomFeatureRight/>
          <CustomFeatureLeft/>
          <Services/>
        </Container>
        <Footer />
      </Section>
    </Main>
  );
}

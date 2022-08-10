import { AppProps } from 'next/app';
import '../styles/globals.css'
import Layout from "../components/Layout";
import { ChakraProvider } from "@chakra-ui/react";
import theme from "../theme";
import UserContextWrapper from "../components/UserContextWrapper";
import Head from "next/head";

function App({ Component, pageProps }: AppProps) {
  return (
    <>
      <Head>
        <title>WINDOTA</title>
      </Head>

      <ChakraProvider theme={theme}>
        <UserContextWrapper>
          <Layout>
            <Component {...pageProps} />
          </Layout>
        </UserContextWrapper>
      </ChakraProvider>
    </>
  );
}

export default App;

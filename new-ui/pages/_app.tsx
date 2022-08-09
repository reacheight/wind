import { AppProps } from 'next/app';
import '../styles/globals.css'
import Layout from "../components/Layout";
import { ChakraProvider } from "@chakra-ui/react";
import theme from "../theme";

function App({ Component, pageProps }: AppProps) {
  return (
    <ChakraProvider theme={theme}>
      <Layout>
        <Component {...pageProps} />
      </Layout>
    </ChakraProvider>);
}

export default App;

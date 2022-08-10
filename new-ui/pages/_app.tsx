import { AppProps } from 'next/app';
import '../styles/globals.css'
import Layout from "../components/Layout";
import { ChakraProvider } from "@chakra-ui/react";
import theme from "../theme";
import UserContextWrapper from "../components/UserContextWrapper";

function App({ Component, pageProps }: AppProps) {
  return (
    <ChakraProvider theme={theme}>
      <UserContextWrapper>
        <Layout>
          <Component {...pageProps} />
        </Layout>
      </UserContextWrapper>
    </ChakraProvider>);
}

export default App;

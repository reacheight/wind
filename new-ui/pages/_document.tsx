import { Html, Head, Main, NextScript } from 'next/document'
import { ColorModeScript } from '@chakra-ui/react';
import theme from "../theme";

const Document = () =>
  <Html>
    <Head>
      <meta name="description" content="Analyze your Dota 2 matches with our AI replay analysis" />
      <link rel="icon" href="/favicon.ico" />
    </Head>
    <body>
      <ColorModeScript initialColorMode={theme.config.initialColorMode} />
      <Main />
      <NextScript />
    </body>
  </Html>

export default Document
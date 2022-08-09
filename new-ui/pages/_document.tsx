import { Html, Head, Main, NextScript } from 'next/document'
import { ColorModeScript } from '@chakra-ui/react';
import theme from "../theme";

const Document = () =>
  <Html>
    <Head>
      <title>WINDOTA</title>
      <meta name="description" content="Dota 2 post match replay analysis" />
      <link rel="icon" href="/favicon.ico" />
    </Head>
    <body>
      <ColorModeScript initialColorMode={theme.config.initialColorMode} />
      <Main />
      <NextScript />
    </body>
  </Html>

export default Document
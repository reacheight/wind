import { extendTheme, type ThemeConfig, type ThemeStyles } from '@chakra-ui/react'
import { mode } from "@chakra-ui/theme-tools";

const config: ThemeConfig = {
  initialColorMode: 'dark',
  useSystemColorMode: false,
}

const styles: ThemeStyles = {
  global: (props) => ({
    body: {
      bg: mode("white", "black")(props),
    }
  })
}

const theme = extendTheme({ config, styles: styles })

export default theme
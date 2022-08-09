import Image from 'next/image'
import Link from "next/link";

const Logo = () =>
  <span>
    <Link href="/"><a><Image src="/windota.svg" alt="Logo" width={170} height={50} /></a></Link>
  </span>

export default Logo
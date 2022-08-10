export default abstract class AccountRoutes {
  public static login = `${process.env.NEXT_PUBLIC_BACKEND_HOST}/login`
  public static logout = `${process.env.NEXT_PUBLIC_BACKEND_HOST}/logout`
  public static user = `${process.env.NEXT_PUBLIC_BACKEND_HOST}/user`
}
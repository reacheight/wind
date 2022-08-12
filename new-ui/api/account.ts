export default abstract class AccountRoutes {
  public static host =  `${process.env.NEXT_PUBLIC_BACKEND_HOST}`
  public static login = `${this.host}/login`
  public static logout = `${this.host}/logout`
  public static user = `${this.host}/user`
  public static matches = `${this.host}/user/matches`
}
package com.example.faketrade.repo

class ValidEndpoints{

    enum class AuthEndpoints(val value: String) {
        Api("/api"), ApiUsers("/api/users"),ApiUserPassword("/api/user/password"),
        ApiUserPasswordReset("/api/user/password_reset"),ApiUserRefreshToken("/api/user/refresh_token"),
        ApiUserLogout("/api/user/logout"),ApiUsersGoogleToken("/api/users/google_token")
    }
    enum class ResourceEndpoints (val value: String){
        UserSaldo("/api/user/saldo")
    }

}

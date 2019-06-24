package configs

data class LabPipeConfig (var tempPath: String) {
    var dbHost: String = "localhost"
    var dbPort: Int = 27017
    var dbName: String = "labpipe-dev"
    var dbUser: String? = null
    var dbPass: String? = null

    var emailHost: String = "localhost"
    var emailPort: Int = 25
    var emailUser: String? = null
    var emailPass: String? = null
}
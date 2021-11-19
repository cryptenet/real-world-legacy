interface FlavorType {
    companion object {
        const val DIMENSION = "target"
        const val DEVELOPMENT = "dev"
        const val PRODUCTION = "prod"
    }

    val suffix: String
    val propFile: String
}

object FlavorTypeDevelopment : FlavorType {
    override val suffix = "dev"
    override val propFile = "dev.properties"
}

object FlavorTypeProduction : FlavorType {
    override val suffix = ""
    override val propFile = "prod.properties"
}

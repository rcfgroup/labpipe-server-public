import uk.ac.le.ember.labpipe.server.cmdline.LPServerCmdLine


fun main(args: Array<String>) {
    if (args.isEmpty()) {
        LPServerCmdLine().main(arrayOf("--help"))
    } else {
        LPServerCmdLine().main(args)
    }
}
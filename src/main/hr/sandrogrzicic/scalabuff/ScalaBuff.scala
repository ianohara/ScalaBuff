package hr.sandrogrzicic.scalabuff

import java.io._
import hr.sandrogrzicic.scalabuff.Parser._

/**
 * ScalaBuff runtime.
 * @author Sandro Gržičić
 */
object ScalaBuff {
	protected var outputDirectory: String = "./"
	protected var importDirectories: Array[String] = Array[String]()

	implicit def stream2reader(stream: InputStream) = new BufferedReader(new InputStreamReader(stream, "utf-8"))

	/**
	 * Runs the ScalaBuff Parser on the specified resource path and returns the output.
	 */
	def apply(resourcePath: String) = {
		var reader: Reader = null
		try {
			reader = new FileInputStream(resourcePath)
		} catch {
			case fnf: FileNotFoundException =>
				reader = new java.net.URL(resourcePath).openStream
			case e => throw e
		}
		Parser(reader).toString
	}

	/**
	 * Runner: Runs the ScalaBuff Parser on the specified resource path(s).
	 */
	def main(args: Array[String]) {
		if (args.length < 1)
			exit(Strings.NEED_INPUT_FILE)

		for (arg <- args) {
			// check if the argument is a potential option
			if (arg.startsWith("-")) {
				option(arg)
			} else {
				// argument is a resource path
				try {
					write(arg.drop(arg.lastIndexOf("/")), apply(arg))
				} catch {
					// just print the error and continue processing other files
					case e => println("Error: Cannot access specified resource [" + arg + "]!)")
				}
			}
		}
	}

	/**
	 * Handle the specified option.
	 */
	protected def option(option: String) {
		if (option == "-h" || option == "--help") {
			println(Strings.HELP)
		} else if (option.startsWith("-I")) {
			importDirectories +:= option.substring("-I".length)
		} else if (option.startsWith("--proto_path=")) {
			importDirectories +:= option.substring("--proto_path=".length)
		} else if (option.startsWith("--scala_out=")) {
			outputDirectory = option.substring("--scala_out=".length)
			if (!outputDirectory.endsWith("/")) {
				outputDirectory += "/"
			}
			if (!(new File(outputDirectory).isDirectory)) {
				exit("Invalid output directory [" + outputDirectory + "]!")
			}
		} else {
			exit("Unknown argument [" + option + "]!")
		}
	}

	/**
	 * Write the specified string to the output directory as a Scala class.
	 */
	protected def write(fileName: String, output: String) {
		new File(outputDirectory + fileName).delete()

		val className = new File(outputDirectory + camelCase(fileName).stripSuffix(".proto") + ".scala")
		val file = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(className), "utf-8"))
		file.write(output)
		file.close()
	}

	lazy val camelCaseRegex = """_(\w)""".r
	def camelCase(str: String) = {
		camelCaseRegex.replaceAllIn(str, m => m.matched.tail.toUpperCase).capitalize
	}


	/**
 	* Print out the specified message and exit.
 	*/
	protected def exit(message: String) {
		println(message)
		System.exit(1)
	}

}
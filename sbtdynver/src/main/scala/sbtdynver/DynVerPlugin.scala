package bleep.plugin.dynver

import java.io.File
import java.util.Date

class DynVerPlugin(
    val baseDirectory: File,
    /* The prefix to use when matching the version tag */
    val dynverTagPrefix: Option[String] = None,
    /* The separator to use between tag and distance, and the hash and dirty timestamp */
    val dynverSeparator: String = DynVer.separator,
    /* The current date, for dynver purposes */
    val dynverCurrentDate: Date = new Date,
    /* Whether to append -SNAPSHOT to snapshot versions */
    val dynverSonatypeSnapshots: Boolean = false
) {

  lazy val tagPrefix = {
    val vTagPrefix = dynverVTagPrefix
    val tagPrefix = dynverTagPrefix.getOrElse(if (vTagPrefix) "v" else "")
    assert(vTagPrefix ^ tagPrefix != "v", s"Incoherence: dynverTagPrefix=$tagPrefix vs dynverVTagPrefix=$vTagPrefix")
    tagPrefix
  }

  /* The dynver instance for this build */
  lazy val dynverInstance: DynVer =
    DynVer(Some(baseDirectory), dynverSeparator, tagPrefix)

  /* Whether or not tags have a 'v' prefix */
  lazy val dynverVTagPrefix: Boolean =
    dynverTagPrefix.getOrElse(DynVer.tagPrefix) == "v"

  /* The output from git describe */
  lazy val dynverGitDescribeOutput: Option[GitDescribeOutput] =
    dynverInstance.getGitDescribeOutput(dynverCurrentDate)

  /* The last stable tag */
  lazy val dynverGitPreviousStableVersion: Option[GitDescribeOutput] =
    dynverInstance.getGitPreviousStableTag

  lazy val isSnapshot: Boolean =
    dynverGitDescribeOutput.isSnapshot

  /* The version string identifies a specific point in version control, so artifacts built from this version can be safely cached */
  lazy val isVersionStable: Boolean =
    dynverGitDescribeOutput.isVersionStable
  /* The last stable version as seen from the current commit (does not include the current commit's version/tag) */
  lazy val previousStableVersion: Option[String] =
    dynverGitPreviousStableVersion.previousVersion

  def getVersion(date: Date, out: Option[GitDescribeOutput]): String =
    out.getVersion(date, dynverSeparator, dynverSonatypeSnapshots)

  // The version of your project, from git
  lazy val dynver: String =
    getVersion(new Date, dynverInstance.getGitDescribeOutput(new Date))

  lazy val version: String =
    getVersion(dynverCurrentDate, dynverGitDescribeOutput)

  // Asserts if the version derives from git tags
  def dynverAssertTagVersion(): Unit =
    dynverGitDescribeOutput.assertTagVersion(version)

  // Checks if version and dynver match
  def dynverCheckVersion: Boolean =
    dynver == version

  // Asserts if version and dynver match
  def dynverAssertVersion(): Unit = {
    val v = version
    val dv = dynver
    if (!dynverCheckVersion)
      sys.error(s"Version and dynver mismatch - version: $v, dynver: $dv")
  }
}

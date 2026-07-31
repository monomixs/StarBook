package com.starbook.core.scanner

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import com.starbook.core.data.repo.ChapterRepoImpl
import com.starbook.core.documentfile.CachedDocumentFile
import com.starbook.core.documentfile.FileBasedDocumentFile
import com.starbook.core.documentfile.nameWithoutExtension
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class ChapterParserTest {

  private val testFolder = TemporaryFolder()

  @Rule
  fun testFolder() = testFolder

  @Before
  fun setUp() {
    testFolder.create()
  }

  @Test
  fun parserSorts() = runTest {
    val audiobook = testFolder.newFolder("audiobook")
    testFolder.newFile("audiobook/Chapter 1.mp3")
    testFolder.newFile("audiobook/Chapter 2.mp3")
    testFolder.newFile("audiobook/Chapter 20.mp3")
    testFolder.newFile("audiobook/Chapter 3.mp3")
    testFolder.newFile("audiobook/Chapter 30.mp3")

    val chapterParser = ChapterParser(
      chapterRepo = ChapterRepoImpl(
        mockk {
          coEvery {
            chapter(any())
          } returns null
          coEvery {
            insert(any())
          } just Runs
        },
      ),
      mediaAnalyzer = mockk {
        coEvery {
          analyze(any())
        } answers {
          val file = firstArg<CachedDocumentFile>()
          Metadata(
            duration = 1000,
            fileName = file.nameWithoutExtension(),
            artist = null,
            album = null,
            chapters = emptyList(),
            title = null,
            genre = null,
            narrator = null,
            series = null,
            part = null,
          )
        }
      },
    )
    assertEquals(
      expected = listOf(
        "Chapter 1",
        "Chapter 2",
        "Chapter 3",
        "Chapter 20",
        "Chapter 30",
      ),
      actual = chapterParser.parse(FileBasedDocumentFile(audiobook))
        .chapters
        .map { it.name },
    )
  }
}


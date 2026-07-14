/*
 * Copyright (c) 2002-2021, the original author(s).
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 *
 * https://opensource.org/licenses/BSD-3-Clause
 */
package org.jline.terminal.impl.jni;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.SequenceInputStream;
import java.nio.charset.Charset;

import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.terminal.spi.SystemStream;
import org.jline.utils.NonBlockingReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class JniTerminalProviderTest {

    @Test
    void testIsSystemStream() {
        assertDoesNotThrow(() -> new JniTerminalProvider().isSystemStream(SystemStream.Output));
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void testNewTerminal() throws IOException {
        PipedOutputStream pos = new PipedOutputStream();
        PipedInputStream pis = new PipedInputStream(pos);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Terminal terminal = new JniTerminalProvider()
                .newTerminal(
                        "name",
                        "xterm",
                        pis,
                        baos,
                        Charset.defaultCharset(),
                        Charset.defaultCharset(),
                        Charset.defaultCharset(),
                        Terminal.SignalHandler.SIG_DFL,
                        true,
                        null,
                        null);
        assertNotNull(terminal);
    }

    @Timeout(5)
    @Test
    void testReadLineOnEmptyInput() throws IOException {
        String fileContent = "";
        InputStream fileStream = new ByteArrayInputStream(fileContent.getBytes());
        Terminal terminal = TerminalBuilder.builder()
                .streams(fileStream, System.err)
                .providers("exec")
                .build();
        LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();
        assertThrows(EndOfFileException.class, () -> reader.readLine("> "));
    }

    @Timeout(5)
    @Test
    void testReadLineAfterEOF() throws IOException {
        System.setProperty(TerminalBuilder.PROP_CLOSE_MODE, "strict");
        String fileContent = "abc\ndef\n";
        InputStream fileStream = new ByteArrayInputStream(fileContent.getBytes());
        InputStream inputStream = new SequenceInputStream(fileStream, new ByteArrayInputStream((new String("\n")).getBytes()));
        Terminal terminal = TerminalBuilder.builder()
                .streams(inputStream, System.err)
                .providers("exec")
                .build();
        LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();

        assertEquals("abc", reader.readLine("> "));
        assertEquals("def", reader.readLine("> "));
        assertEquals("", reader.readLine("> "));
        assertThrows(EndOfFileException.class, () -> reader.readLine("> "));
    }

    @Timeout(5)
    @Test
    void testReadLineOnEmptyInputWithoutExec() throws IOException {
        String fileContent = "";
        InputStream fileStream = new ByteArrayInputStream(fileContent.getBytes());
        Terminal terminal = TerminalBuilder.builder()
                .streams(fileStream, System.err)
                .build();
        LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();
        assertThrows(EndOfFileException.class, () -> reader.readLine("> "));
    }

    @Timeout(5)
    @Test
    void testReadLineAfterEOFWithoutExec() throws IOException {
        System.setProperty(TerminalBuilder.PROP_CLOSE_MODE, "strict");
        String fileContent = "abc\ndef\n";
        InputStream fileStream = new ByteArrayInputStream(fileContent.getBytes());
        InputStream inputStream = new SequenceInputStream(fileStream, new ByteArrayInputStream((new String("\n")).getBytes()));
        Terminal terminal = TerminalBuilder.builder()
                .streams(inputStream, System.err)
                .build();
        LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();

        assertEquals("abc", reader.readLine("> "));
        assertEquals("def", reader.readLine("> "));
        assertEquals("", reader.readLine("> "));
        assertThrows(EndOfFileException.class, () -> reader.readLine("> "));
    }
}

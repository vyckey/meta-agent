/*
 * MIT License
 *
 * Copyright (c) 2025 MetaAgent
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.metaagent.framework.tools.script.shell;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ShellCommandUtils class
 *
 * @author vyckey
 */
class ShellCommandUtilsTest {

    @Test
    void testSplitCommands_EmptyOrNullInput() {
        // Test null input
        List<String> result = ShellCommandUtils.splitCommands(null);
        assertTrue(result.isEmpty());

        // Test empty input
        result = ShellCommandUtils.splitCommands("");
        assertTrue(result.isEmpty());

        // Test whitespace only input
        result = ShellCommandUtils.splitCommands("   ");
        assertTrue(result.isEmpty());
    }

    @Test
    void testSplitCommands_SingleCommand() {
        // Test simple command
        List<String> result = ShellCommandUtils.splitCommands("ls");
        assertEquals(1, result.size());
        assertEquals("ls", result.get(0));

        // Test command with arguments
        result = ShellCommandUtils.splitCommands("ls -la /tmp");
        assertEquals(1, result.size());
        assertEquals("ls -la /tmp", result.get(0));
    }

    @Test
    void testSplitCommands_MultipleCommands() {
        // Test commands separated by semicolon
        List<String> result = ShellCommandUtils.splitCommands("ls; pwd");
        assertEquals(2, result.size());
        assertEquals("ls", result.get(0));
        assertEquals("pwd", result.get(1));

        // Test commands separated by &&
        result = ShellCommandUtils.splitCommands("ls && pwd");
        assertEquals(2, result.size());
        assertEquals("ls", result.get(0));
        assertEquals("pwd", result.get(1));

        // Test commands separated by ||
        result = ShellCommandUtils.splitCommands("ls || pwd");
        assertEquals(2, result.size());
        assertEquals("ls", result.get(0));
        assertEquals("pwd", result.get(1));

        // Test multiple separators
        result = ShellCommandUtils.splitCommands("ls && pwd; echo hello");
        assertEquals(3, result.size());
        assertEquals("ls", result.get(0));
        assertEquals("pwd", result.get(1));
        assertEquals("echo hello", result.get(2));
    }

    @Test
    void testSplitCommands_CommandsWithQuotes() {
        // Test command with double quotes
        List<String> result = ShellCommandUtils.splitCommands("echo \"Hello World\" && ls");
        assertEquals(2, result.size());
        assertEquals("echo \"Hello World\"", result.get(0));
        assertEquals("ls", result.get(1));

        // Test command with single quotes
        result = ShellCommandUtils.splitCommands("mkdir 'New Folder' ; ls");
        assertEquals(2, result.size());
        assertEquals("mkdir 'New Folder'", result.get(0));
        assertEquals("ls", result.get(1));

        // Test command with mixed quotes
        result = ShellCommandUtils.splitCommands("echo \"Hello 'World'\" || echo 'Hello \"World\"'");
        assertEquals(2, result.size());
        assertEquals("echo \"Hello 'World'\"", result.get(0));
        assertEquals("echo 'Hello \"World\"'", result.get(1));
    }

    @Test
    void testSplitCommands_CommandsWithEscapedCharacters() {
        // Test command with escaped characters
        List<String> result = ShellCommandUtils.splitCommands("echo Hello\\ World && ls");
        assertEquals(2, result.size());
        assertEquals("echo Hello\\ World", result.get(0));
        assertEquals("ls", result.get(1));
    }

    @Test
    void testGetCommandRoot_NullOrEmptyInput() {
        // Test null input
        assertNull(ShellCommandUtils.getCommandRoot(null));

        // Test empty input
        assertNull(ShellCommandUtils.getCommandRoot(""));

        // Test whitespace only input
        assertNull(ShellCommandUtils.getCommandRoot("   "));
    }

    @Test
    void testGetCommandRoot_SimpleCommands() {
        // Test simple command
        assertEquals("ls", ShellCommandUtils.getCommandRoot("ls"));
        assertEquals("ls", ShellCommandUtils.getCommandRoot("ls -la /tmp"));

        // Test command with multiple arguments
        assertEquals("git", ShellCommandUtils.getCommandRoot("git commit -m \"Initial commit\""));
    }

    @Test
    void testGetCommandRoot_PathCommands() {
        // Test command with path
        assertEquals("python", ShellCommandUtils.getCommandRoot("/usr/bin/python script.py"));
        assertEquals("bash", ShellCommandUtils.getCommandRoot("/bin/bash script.sh"));
    }

    @Test
    void testGetCommandRoot_QuotedCommands() {
        // Test command with double quotes
        assertEquals("python.exe", ShellCommandUtils.getCommandRoot("\"C:\\Users\\My User\\python.exe\" script.py"));

        // Test command with single quotes
        assertEquals("python", ShellCommandUtils.getCommandRoot("'python' script.py"));
    }

    @Test
    void testHasCommandSubstitution_NullOrEmptyInput() {
        // Test null input
        assertFalse(ShellCommandUtils.hasCommandSubstitution(null));

        // Test empty input
        assertFalse(ShellCommandUtils.hasCommandSubstitution(""));

        // Test whitespace only input
        assertFalse(ShellCommandUtils.hasCommandSubstitution("   "));
    }

    @Test
    void testHasCommandSubstitution_NoSubstitution() {
        // Test command without substitution
        assertFalse(ShellCommandUtils.hasCommandSubstitution("ls -la"));
        assertFalse(ShellCommandUtils.hasCommandSubstitution("echo 'Hello World'"));
    }

    @Test
    void testHasCommandSubstitution_DollarParentheses() {
        // Test $(...) substitution
        assertTrue(ShellCommandUtils.hasCommandSubstitution("ls $(pwd)"));
        assertTrue(ShellCommandUtils.hasCommandSubstitution("echo \"Today is $(date)\""));
        
        // Test escaped $(...)
        assertFalse(ShellCommandUtils.hasCommandSubstitution("echo \\$(pwd)")); // Escaped, so not a substitution
        
        // Test $(...) in double quotes
        assertTrue(ShellCommandUtils.hasCommandSubstitution("echo \"Files: $(ls)\""));
        
        // Test $(...) in single quotes (should not be recognized)
        assertFalse(ShellCommandUtils.hasCommandSubstitution("echo 'Files: $(ls)'")); // In single quotes, so not a substitution
    }

    @Test
    void testHasCommandSubstitution_Backticks() {
        // Test backtick substitution
        assertTrue(ShellCommandUtils.hasCommandSubstitution("ls `pwd`"));
        assertTrue(ShellCommandUtils.hasCommandSubstitution("echo \"Today is `date`\""));
        
        // Test escaped backticks
        assertFalse(ShellCommandUtils.hasCommandSubstitution("echo \\`pwd\\`")); // Escaped, so not a substitution
        
        // Test backticks in double quotes
        assertTrue(ShellCommandUtils.hasCommandSubstitution("echo \"Files: `ls`\""));
        
        // Test backticks in single quotes (should not be recognized)
        assertFalse(ShellCommandUtils.hasCommandSubstitution("echo 'Files: `ls`'")); // In single quotes, so not a substitution
    }

    @Test
    void testHasCommandSubstitution_ProcessSubstitution() {
        // Test <(...) process substitution
        assertTrue(ShellCommandUtils.hasCommandSubstitution("diff <(ls /bin) <(ls /usr/bin)"));
        
        // Test <(...) in double quotes
        assertFalse(ShellCommandUtils.hasCommandSubstitution("echo \"<(ls)\"")); // In double quotes, so not a process substitution
        
        // Test <(...) in single quotes
        assertFalse(ShellCommandUtils.hasCommandSubstitution("echo '<(ls)'")); // In single quotes, so not a process substitution
    }

    @Test
    void testHasCommandSubstitution_ComplexCases() {
        // Test mixed substitutions
        assertTrue(ShellCommandUtils.hasCommandSubstitution("echo $(date): `whoami`"));
        
        // Test nested substitutions
        assertTrue(ShellCommandUtils.hasCommandSubstitution("echo $(echo `date`)"));
    }
}
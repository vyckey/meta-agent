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

package org.metaagent.framework.core.common.security.approver;

import java.util.Scanner;

/**
 * Terminal Human Approver
 *
 * @author vyckey
 */
public class TerminalHumanApprover implements HumanApprover {
    public static final TerminalHumanApprover INSTANCE = new TerminalHumanApprover();

    private TerminalHumanApprover() {
    }

    @Override
    public HumanApprovalOutput request(HumanApprovalInput input) {
        String toolName = input.metadata().getProperty(HumanApprovalInput.METADATA_TOOL_NAME, String.class);
        if (toolName != null) {
            System.out.println("Tool " + toolName + " is requesting approval.");
        }
        System.out.println(input.content() + "\nPlease approve the above request, enter Y/n:");
        Scanner scanner = new Scanner(System.in);
        do {
            String confirmation = scanner.nextLine().trim();
            if ("y".equalsIgnoreCase(confirmation) || "n".equalsIgnoreCase(confirmation)) {
                if ("y".equalsIgnoreCase(confirmation)) {
                    return new HumanApprovalOutput(HumanApprovalStatus.APPROVED, null);
                } else {
                    return new HumanApprovalOutput(HumanApprovalStatus.REJECTED, "User reject approval");
                }
            }
            System.out.println("Please enter Y or n:");
        } while (true);
    }

}

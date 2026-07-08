/*
 * Copyright (c) 2026, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package compiler.c2.irTests;

import compiler.lib.ir_framework.*;
import jdk.incubator.vector.*;
import jdk.internal.misc.Unsafe;
import jdk.test.lib.Asserts;
import java.util.Arrays;

/*
 * @test
 * @bug 8387472
 * @summary Test removal of a redundant smaller StoreVector fully covered
 *          by a later wider StoreVector at a different address.
 * @library /test/lib /
 * @modules jdk.incubator.vector
 * @run driver ${test.main.class}
 */

public class TestRemoveFullyCoveredStores {
    static final Unsafe UNSAFE = Unsafe.getUnsafe();

    static final byte[] BYTES = new byte[16];
    static final long BYTE_BASE = UNSAFE.arrayBaseOffset(byte[].class);

    static final int[] DST = new int[8];
    static final int[] SRC_A = new int[8];
    static final int[] SRC_B = new int[8];

    static final VectorSpecies<Integer> I64  = IntVector.SPECIES_64;
    static final VectorSpecies<Integer> I128 = IntVector.SPECIES_128;

    static {
        for (int i = 0; i < SRC_A.length; i++) {
            SRC_A[i] = 1000 + i;
            SRC_B[i] = 2000 + i;
        }
    }

    public static void main(String[] args) {
        TestFramework.run();
    }

    /*
     * StoreI [base + 4, base + 8)
     * StoreL [base + 0, base + 8)
     *
     * The later StoreL fully covers the earlier StoreI even though the stores
     * use different addresses.
     */
    @Test
    @IR(counts = {IRNode.STORE_I, "0", IRNode.STORE_L, "1"},
        phase = CompilePhase.BEFORE_MATCHING)
    public static void testStoreLongCoversStoreInt() {
        UNSAFE.putInt(BYTES, BYTE_BASE + 4, 0x12345678);
        UNSAFE.putLong(BYTES, BYTE_BASE, 0x1122334455667788L);
    }

    @Run(test = "testStoreLongCoversStoreInt")
    @Warmup(0)
    public static void runStoreLongCoversStoreInt() {
        Arrays.fill(BYTES, (byte)0);
        testStoreLongCoversStoreInt();
        Asserts.assertEQ(UNSAFE.getLong(BYTES, BYTE_BASE),
                         0x1122334455667788L);
    }

    /*
     * StoreVector64  DST[1..2]
     * byte range: [base + 4, base + 12)
     *
     * StoreVector128 DST[0..3]
     * byte range: [base + 0, base + 16)
     *
     * The later wider StoreVector fully covers the earlier smaller StoreVector
     * even though the stores use different addresses.
     */
    @Test
    @IR(counts = {IRNode.STORE_VECTOR, "1"},
        phase = CompilePhase.BEFORE_MATCHING,
        applyIf = {"MaxVectorSize", ">= 16"},
        applyIfCPUFeatureOr = {"asimd", "true", "avx", "true", "rvv", "true"})
    public static void testWiderStoreVectorCoversSmallerStoreVector() {
        IntVector.fromArray(I64, SRC_A, 0).intoArray(DST, 1);
        IntVector.fromArray(I128, SRC_B, 0).intoArray(DST, 0);
    }

    @Run(test = "testWiderStoreVectorCoversSmallerStoreVector")
    @Warmup(0)
    public static void runWiderStoreVectorCoversSmallerStoreVector() {
        Arrays.fill(DST, -1);
        testWiderStoreVectorCoversSmallerStoreVector();
        verifyVectorResult(I128.length());
    }

    @DontInline
    private static void verifyLongResult(long expected) {
        Asserts.assertEquals(UNSAFE.getLong(BYTES, BYTE_BASE), expected);
    }
}

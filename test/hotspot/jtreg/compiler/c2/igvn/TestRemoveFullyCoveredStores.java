/*
 * @test
 * @summary Fixed generated tests for segment.set(..._UNALIGNED) mixed with vector/Unsafe stores
 * @requires vm.compiler2.enabled
 * @library /test/lib
 * @modules java.base/jdk.internal.misc
 *          jdk.incubator.vector
 *
 * @run main/othervm/timeout=300
 *      --add-modules=jdk.incubator.vector
 *      --add-exports=java.base/jdk.internal.misc=ALL-UNNAMED
 *      -XX:+UnlockDiagnosticVMOptions
 *      -XX:-TieredCompilation
 *      -XX:CompileThreshold=100
 *      -XX:-BackgroundCompilation
 *      -Xbatch
 *      -XX:CompileCommand=compileonly,compiler.c2.igvn.TestRemoveFullyCoveredStores::test*
 *      -XX:CompileCommand=exclude,compiler.c2.igvn.TestRemoveFullyCoveredStores::reference*
 *      compiler.c2.igvn.TestRemoveFullyCoveredStores
 */

package compiler.c2.igvn;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.Arrays;

import jdk.incubator.vector.Float16Vector;
import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.LongVector;
import jdk.incubator.vector.ShortVector;
import jdk.incubator.vector.VectorMask;
import jdk.internal.misc.Unsafe;

public class TestRemoveFullyCoveredStores {
    private static final ValueLayout.OfShort SHORT_UNALIGNED = ValueLayout.JAVA_SHORT.withByteAlignment(1);
    private static final ValueLayout.OfInt INT_UNALIGNED = ValueLayout.JAVA_INT.withByteAlignment(1);
    private static final ValueLayout.OfLong LONG_UNALIGNED = ValueLayout.JAVA_LONG.withByteAlignment(1);

    private static final int ITERS = 20_000;

    private static volatile int sinkI;
    private static volatile long sinkL;

    static final class UnsafeHolder {
        static final Unsafe UNSAFE = Unsafe.getUnsafe();
        static final long SHORT_BASE = UNSAFE.arrayBaseOffset(short[].class);
        static final long INT_BASE = UNSAFE.arrayBaseOffset(int[].class);
        static final long LONG_BASE = UNSAFE.arrayBaseOffset(long[].class);
    }

    public static void main(String[] args) {
        checkShort("linuxFloat16128POSITIVErandomMixedStores", referenceLinuxFloat16128POSITIVErandomMixedStores(),
                   TestRemoveFullyCoveredStores::testLinuxFloat16128POSITIVErandomMixedStores);
        checkLong("linuxLong128NEGATIVErandomMixedStores", referenceLinuxLong128NEGATIVErandomMixedStores(),
                  TestRemoveFullyCoveredStores::testLinuxLong128NEGATIVErandomMixedStores);
        checkShort("windowsFloat16128POSITIVErandomMixedStores", referenceWindowsFloat16128POSITIVErandomMixedStores(),
                   TestRemoveFullyCoveredStores::testWindowsFloat16128POSITIVErandomMixedStores);
        checkInt("windowsInteger128NEGATIVErandomMixedStores", referenceWindowsInteger128NEGATIVErandomMixedStores(),
                 TestRemoveFullyCoveredStores::testWindowsInteger128NEGATIVErandomMixedStores);
        checkLong("windowsLong128NEGATIVErandomMixedStores", referenceWindowsLong128NEGATIVErandomMixedStores(),
                  TestRemoveFullyCoveredStores::testWindowsLong128NEGATIVErandomMixedStores);
        checkShort("macOrConfirmedShort64NEGATIVErandomMixedStores", referenceMacOrConfirmedShort64NEGATIVErandomMixedStores(),
                   TestRemoveFullyCoveredStores::testMacOrConfirmedShort64NEGATIVErandomMixedStores);
    }

    @FunctionalInterface
    interface ShortTest { short[] run(); }

    @FunctionalInterface
    interface IntTest { int[] run(); }

    @FunctionalInterface
    interface LongTest { long[] run(); }

    private static void checkShort(String name, short[] expected, ShortTest test) {
        for (int i = 0; i < ITERS; i++) {
            short[] actual = test.run();
            if (!Arrays.equals(expected, actual)) {
                throw new RuntimeException("wrong result for " + name + ":\n" +
                        "  interpreter result: " + Arrays.toString(expected) + "\n" +
                        "  compiled result:    " + Arrays.toString(actual));
            }
            sinkI += actual[i & 127];
        }
    }

    private static void checkInt(String name, int[] expected, IntTest test) {
        for (int i = 0; i < ITERS; i++) {
            int[] actual = test.run();
            if (!Arrays.equals(expected, actual)) {
                throw new RuntimeException("wrong result for " + name + ":\n" +
                        "  interpreter result: " + Arrays.toString(expected) + "\n" +
                        "  compiled result:    " + Arrays.toString(actual));
            }
            sinkI += actual[i & 127];
        }
    }

    private static void checkLong(String name, long[] expected, LongTest test) {
        for (int i = 0; i < ITERS; i++) {
            long[] actual = test.run();
            if (!Arrays.equals(expected, actual)) {
                throw new RuntimeException("wrong result for " + name + ":\n" +
                        "  interpreter result: " + Arrays.toString(expected) + "\n" +
                        "  compiled result:    " + Arrays.toString(actual));
            }
            sinkL += actual[i & 127];
        }
    }

    static short[] testLinuxFloat16128POSITIVErandomMixedStores() {
        short[] array = new short[128];
        MemorySegment segment = MemorySegment.ofArray(array);

        array[4] = (short)0xb80c;
        Float16Vector.broadcast(Float16Vector.SPECIES_128, (short)0x6ab1).intoArray(array, 0, new int[] {2, 61, 10, 103, 69, 16, 109, 78}, 0);
        Float16Vector.broadcast(Float16Vector.SPECIES_128, (short)0x7d95).intoArray(array, 94, VectorMask.fromLong(Float16Vector.SPECIES_128, -129L));
        Float16Vector.broadcast(Float16Vector.SPECIES_128, (short)0x3890).intoArray(array, 17, new int[] {2, 28, 90, 90, 62, 34, 40, 11}, 0, VectorMask.fromLong(Float16Vector.SPECIES_128, -2L));
        UnsafeHolder.UNSAFE.putShort(array, UnsafeHolder.SHORT_BASE + 188, (short)0x77de);
        Float16Vector.broadcast(Float16Vector.SPECIES_128, (short)0x1eb).intoArray(array, 0, VectorMask.fromLong(Float16Vector.SPECIES_128, -2L));
        Float16Vector.broadcast(Float16Vector.SPECIES_128, (short)0xc3d1).intoArray(array, 0);
        Float16Vector.broadcast(Float16Vector.SPECIES_128, (short)0x8d9a).intoArray(array, 58);
        segment.set(SHORT_UNALIGNED, 76, (short)0x6701);
        array[105] = (short)0xfba6;
        segment.set(SHORT_UNALIGNED, 3, (short)0x7edc);
        UnsafeHolder.UNSAFE.putShort(array, UnsafeHolder.SHORT_BASE + 4, (short)0x6d99);
        return array;
    }

    static short[] referenceLinuxFloat16128POSITIVErandomMixedStores() {
        short[] array = new short[128];
        MemorySegment segment = MemorySegment.ofArray(array);

        array[4] = (short)0xb80c;
        Float16Vector.broadcast(Float16Vector.SPECIES_128, (short)0x6ab1).intoArray(array, 0, new int[] {2, 61, 10, 103, 69, 16, 109, 78}, 0);
        Float16Vector.broadcast(Float16Vector.SPECIES_128, (short)0x7d95).intoArray(array, 94, VectorMask.fromLong(Float16Vector.SPECIES_128, -129L));
        Float16Vector.broadcast(Float16Vector.SPECIES_128, (short)0x3890).intoArray(array, 17, new int[] {2, 28, 90, 90, 62, 34, 40, 11}, 0, VectorMask.fromLong(Float16Vector.SPECIES_128, -2L));
        UnsafeHolder.UNSAFE.putShort(array, UnsafeHolder.SHORT_BASE + 188, (short)0x77de);
        Float16Vector.broadcast(Float16Vector.SPECIES_128, (short)0x1eb).intoArray(array, 0, VectorMask.fromLong(Float16Vector.SPECIES_128, -2L));
        Float16Vector.broadcast(Float16Vector.SPECIES_128, (short)0xc3d1).intoArray(array, 0);
        Float16Vector.broadcast(Float16Vector.SPECIES_128, (short)0x8d9a).intoArray(array, 58);
        segment.set(SHORT_UNALIGNED, 76, (short)0x6701);
        array[105] = (short)0xfba6;
        segment.set(SHORT_UNALIGNED, 3, (short)0x7edc);
        UnsafeHolder.UNSAFE.putShort(array, UnsafeHolder.SHORT_BASE + 4, (short)0x6d99);
        return array;
    }

    static long[] testLinuxLong128NEGATIVErandomMixedStores() {
        long[] array = new long[128];
        MemorySegment segment = MemorySegment.ofArray(array);

        LongVector.broadcast(LongVector.SPECIES_128, 0x2b04eeed27c8a98eL).intoArray(array, 0, new int[] {127, 22}, 0);
        array[1] = 0x7f151fb81d4ee5b0L;
        UnsafeHolder.UNSAFE.putLong(array, UnsafeHolder.LONG_BASE + 736, 0x137c323d7f74a73aL);
        array[125] = 0x6ec41dfca6b8acc3L;
        UnsafeHolder.UNSAFE.putLong(array, UnsafeHolder.LONG_BASE + 0, 0xc38a7d613defa7e0L);
        LongVector.broadcast(LongVector.SPECIES_128, 0x39e2c2fa30bed5b7L).intoArray(array, 0, VectorMask.fromLong(LongVector.SPECIES_128, -3L));
        LongVector.broadcast(LongVector.SPECIES_128, 0xb2a7886655d62d3cL).intoArray(array, 22, VectorMask.fromLong(LongVector.SPECIES_128, -3L));
        LongVector.broadcast(LongVector.SPECIES_128, 0x1f4b2d53e0b3d2bbL).intoArray(array, 41);
        segment.set(LONG_UNALIGNED, 2, 0x3ee295b12346f814L);
        LongVector.broadcast(LongVector.SPECIES_128, 0x7d31d843b5ee4182L).intoArray(array, 0);
        segment.set(LONG_UNALIGNED, 541, 0x99e26c1775321c2L);
        LongVector.broadcast(LongVector.SPECIES_128, 0x4b88a8db5b4029a4L).intoArray(array, 94, new int[] {22, 16}, 0, VectorMask.fromLong(LongVector.SPECIES_128, -2L));
        return array;
    }

    static long[] referenceLinuxLong128NEGATIVErandomMixedStores() {
        long[] array = new long[128];
        MemorySegment segment = MemorySegment.ofArray(array);

        LongVector.broadcast(LongVector.SPECIES_128, 0x2b04eeed27c8a98eL).intoArray(array, 0, new int[] {127, 22}, 0);
        array[1] = 0x7f151fb81d4ee5b0L;
        UnsafeHolder.UNSAFE.putLong(array, UnsafeHolder.LONG_BASE + 736, 0x137c323d7f74a73aL);
        array[125] = 0x6ec41dfca6b8acc3L;
        UnsafeHolder.UNSAFE.putLong(array, UnsafeHolder.LONG_BASE + 0, 0xc38a7d613defa7e0L);
        LongVector.broadcast(LongVector.SPECIES_128, 0x39e2c2fa30bed5b7L).intoArray(array, 0, VectorMask.fromLong(LongVector.SPECIES_128, -3L));
        LongVector.broadcast(LongVector.SPECIES_128, 0xb2a7886655d62d3cL).intoArray(array, 22, VectorMask.fromLong(LongVector.SPECIES_128, -3L));
        LongVector.broadcast(LongVector.SPECIES_128, 0x1f4b2d53e0b3d2bbL).intoArray(array, 41);
        segment.set(LONG_UNALIGNED, 2, 0x3ee295b12346f814L);
        LongVector.broadcast(LongVector.SPECIES_128, 0x7d31d843b5ee4182L).intoArray(array, 0);
        segment.set(LONG_UNALIGNED, 541, 0x99e26c1775321c2L);
        LongVector.broadcast(LongVector.SPECIES_128, 0x4b88a8db5b4029a4L).intoArray(array, 94, new int[] {22, 16}, 0, VectorMask.fromLong(LongVector.SPECIES_128, -2L));
        return array;
    }

    static short[] testWindowsFloat16128POSITIVErandomMixedStores() {
        short[] array = new short[128];
        MemorySegment segment = MemorySegment.ofArray(array);

        UnsafeHolder.UNSAFE.putShort(array, UnsafeHolder.SHORT_BASE + 186, (short)0x3d1e);
        Float16Vector.broadcast(Float16Vector.SPECIES_128, (short)0x448c).intoArray(array, 38);
        Float16Vector.broadcast(Float16Vector.SPECIES_128, (short)0x1fed).intoArray(array, 0);
        UnsafeHolder.UNSAFE.putShort(array, UnsafeHolder.SHORT_BASE + 14, (short)0x8c32);
        Float16Vector.broadcast(Float16Vector.SPECIES_128, (short)0xaa95).intoArray(array, 0, new int[] {102, 125, 89, 115, 92, 42, 74, 103}, 0);
        array[60] = (short)0x657;
        Float16Vector.broadcast(Float16Vector.SPECIES_128, (short)0x6fa6).intoArray(array, 0, VectorMask.fromLong(Float16Vector.SPECIES_128, -5L));
        Float16Vector.broadcast(Float16Vector.SPECIES_128, (short)0x3de3).intoArray(array, 17, VectorMask.fromLong(Float16Vector.SPECIES_128, -2L));
        segment.set(SHORT_UNALIGNED, 4, (short)0x68d);
        segment.set(SHORT_UNALIGNED, 146, (short)0x3084);
        Float16Vector.broadcast(Float16Vector.SPECIES_128, (short)0x6c7b).intoArray(array, 92, new int[] {21, 3, 20, 12, 12, 4, 34, 10}, 0, VectorMask.fromLong(Float16Vector.SPECIES_128, -129L));
        array[5] = (short)0x33f5;
        return array;
    }

    static short[] referenceWindowsFloat16128POSITIVErandomMixedStores() {
        short[] array = new short[128];
        MemorySegment segment = MemorySegment.ofArray(array);

        UnsafeHolder.UNSAFE.putShort(array, UnsafeHolder.SHORT_BASE + 186, (short)0x3d1e);
        Float16Vector.broadcast(Float16Vector.SPECIES_128, (short)0x448c).intoArray(array, 38);
        Float16Vector.broadcast(Float16Vector.SPECIES_128, (short)0x1fed).intoArray(array, 0);
        UnsafeHolder.UNSAFE.putShort(array, UnsafeHolder.SHORT_BASE + 14, (short)0x8c32);
        Float16Vector.broadcast(Float16Vector.SPECIES_128, (short)0xaa95).intoArray(array, 0, new int[] {102, 125, 89, 115, 92, 42, 74, 103}, 0);
        array[60] = (short)0x657;
        Float16Vector.broadcast(Float16Vector.SPECIES_128, (short)0x6fa6).intoArray(array, 0, VectorMask.fromLong(Float16Vector.SPECIES_128, -5L));
        Float16Vector.broadcast(Float16Vector.SPECIES_128, (short)0x3de3).intoArray(array, 17, VectorMask.fromLong(Float16Vector.SPECIES_128, -2L));
        segment.set(SHORT_UNALIGNED, 4, (short)0x68d);
        segment.set(SHORT_UNALIGNED, 146, (short)0x3084);
        Float16Vector.broadcast(Float16Vector.SPECIES_128, (short)0x6c7b).intoArray(array, 92, new int[] {21, 3, 20, 12, 12, 4, 34, 10}, 0, VectorMask.fromLong(Float16Vector.SPECIES_128, -129L));
        array[5] = (short)0x33f5;
        return array;
    }

    static int[] testWindowsInteger128NEGATIVErandomMixedStores() {
        int[] array = new int[128];
        MemorySegment segment = MemorySegment.ofArray(array);

        segment.set(INT_UNALIGNED, 6, 0x24835572);
        array[2] = 0x97f9b772;
        IntVector.broadcast(IntVector.SPECIES_128, 0x6ea614e3).intoArray(array, 36, VectorMask.fromLong(IntVector.SPECIES_128, -3L));
        IntVector.broadcast(IntVector.SPECIES_128, 0x629cf8e).intoArray(array, 0, VectorMask.fromLong(IntVector.SPECIES_128, -9L));
        UnsafeHolder.UNSAFE.putInt(array, UnsafeHolder.INT_BASE + 496, 0x2293c472);
        IntVector.broadcast(IntVector.SPECIES_128, 0x3ec27c0a).intoArray(array, 21, new int[] {63, 69, 92, 22}, 0, VectorMask.fromLong(IntVector.SPECIES_128, -3L));
        array[71] = 0x5df2e8dc;
        IntVector.broadcast(IntVector.SPECIES_128, 0x4b3b7117).intoArray(array, 95);
        segment.set(INT_UNALIGNED, 97, 0xb904d6cb);
        UnsafeHolder.UNSAFE.putInt(array, UnsafeHolder.INT_BASE + 4, 0x314b0d0b);
        IntVector.broadcast(IntVector.SPECIES_128, 0x4e83dba).intoArray(array, 0);
        IntVector.broadcast(IntVector.SPECIES_128, 0xf1cee5c3).intoArray(array, 0, new int[] {112, 87, 73, 70}, 0);
        return array;
    }

    static int[] referenceWindowsInteger128NEGATIVErandomMixedStores() {
        int[] array = new int[128];
        MemorySegment segment = MemorySegment.ofArray(array);

        segment.set(INT_UNALIGNED, 6, 0x24835572);
        array[2] = 0x97f9b772;
        IntVector.broadcast(IntVector.SPECIES_128, 0x6ea614e3).intoArray(array, 36, VectorMask.fromLong(IntVector.SPECIES_128, -3L));
        IntVector.broadcast(IntVector.SPECIES_128, 0x629cf8e).intoArray(array, 0, VectorMask.fromLong(IntVector.SPECIES_128, -9L));
        UnsafeHolder.UNSAFE.putInt(array, UnsafeHolder.INT_BASE + 496, 0x2293c472);
        IntVector.broadcast(IntVector.SPECIES_128, 0x3ec27c0a).intoArray(array, 21, new int[] {63, 69, 92, 22}, 0, VectorMask.fromLong(IntVector.SPECIES_128, -3L));
        array[71] = 0x5df2e8dc;
        IntVector.broadcast(IntVector.SPECIES_128, 0x4b3b7117).intoArray(array, 95);
        segment.set(INT_UNALIGNED, 97, 0xb904d6cb);
        UnsafeHolder.UNSAFE.putInt(array, UnsafeHolder.INT_BASE + 4, 0x314b0d0b);
        IntVector.broadcast(IntVector.SPECIES_128, 0x4e83dba).intoArray(array, 0);
        IntVector.broadcast(IntVector.SPECIES_128, 0xf1cee5c3).intoArray(array, 0, new int[] {112, 87, 73, 70}, 0);
        return array;
    }

    static long[] testWindowsLong128NEGATIVErandomMixedStores() {
        long[] array = new long[128];
        MemorySegment segment = MemorySegment.ofArray(array);

        LongVector.broadcast(LongVector.SPECIES_128, 0x8b8701f3dd4beaf9L).intoArray(array, 84);
        segment.set(LONG_UNALIGNED, 603, 0xa2b5c059cb4de533L);
        LongVector.broadcast(LongVector.SPECIES_128, 0x5b513dc8525ae8dfL).intoArray(array, 0, new int[] {84, 57}, 0);
        LongVector.broadcast(LongVector.SPECIES_128, 0xab28befdfcef037aL).intoArray(array, 29, new int[] {73, 33}, 0, VectorMask.fromLong(LongVector.SPECIES_128, -2L));
        LongVector.broadcast(LongVector.SPECIES_128, 0x67b3511efa39cf8dL).intoArray(array, 99, VectorMask.fromLong(LongVector.SPECIES_128, -2L));
        UnsafeHolder.UNSAFE.putLong(array, UnsafeHolder.LONG_BASE + 832, 0xfbf216c2348b2317L);
        LongVector.broadcast(LongVector.SPECIES_128, 0xd3d6e982b4c9fa1dL).intoArray(array, 0, VectorMask.fromLong(LongVector.SPECIES_128, -3L));
        UnsafeHolder.UNSAFE.putLong(array, UnsafeHolder.LONG_BASE + 8, 0x9fdb4cea9735001cL);
        array[0] = 0x7cf3969aab6ed029L;
        segment.set(LONG_UNALIGNED, 1, 0xb753a6ad6717ffacL);
        array[88] = 0xed03ab1d15923162L;
        LongVector.broadcast(LongVector.SPECIES_128, 0x63a36d31fe0f664bL).intoArray(array, 0);
        return array;
    }

    static long[] referenceWindowsLong128NEGATIVErandomMixedStores() {
        long[] array = new long[128];
        MemorySegment segment = MemorySegment.ofArray(array);

        LongVector.broadcast(LongVector.SPECIES_128, 0x8b8701f3dd4beaf9L).intoArray(array, 84);
        segment.set(LONG_UNALIGNED, 603, 0xa2b5c059cb4de533L);
        LongVector.broadcast(LongVector.SPECIES_128, 0x5b513dc8525ae8dfL).intoArray(array, 0, new int[] {84, 57}, 0);
        LongVector.broadcast(LongVector.SPECIES_128, 0xab28befdfcef037aL).intoArray(array, 29, new int[] {73, 33}, 0, VectorMask.fromLong(LongVector.SPECIES_128, -2L));
        LongVector.broadcast(LongVector.SPECIES_128, 0x67b3511efa39cf8dL).intoArray(array, 99, VectorMask.fromLong(LongVector.SPECIES_128, -2L));
        UnsafeHolder.UNSAFE.putLong(array, UnsafeHolder.LONG_BASE + 832, 0xfbf216c2348b2317L);
        LongVector.broadcast(LongVector.SPECIES_128, 0xd3d6e982b4c9fa1dL).intoArray(array, 0, VectorMask.fromLong(LongVector.SPECIES_128, -3L));
        UnsafeHolder.UNSAFE.putLong(array, UnsafeHolder.LONG_BASE + 8, 0x9fdb4cea9735001cL);
        array[0] = 0x7cf3969aab6ed029L;
        segment.set(LONG_UNALIGNED, 1, 0xb753a6ad6717ffacL);
        array[88] = 0xed03ab1d15923162L;
        LongVector.broadcast(LongVector.SPECIES_128, 0x63a36d31fe0f664bL).intoArray(array, 0);
        return array;
    }

    static short[] testMacOrConfirmedShort64NEGATIVErandomMixedStores() {
        short[] array = new short[128];
        MemorySegment segment = MemorySegment.ofArray(array);

        UnsafeHolder.UNSAFE.putShort(array, UnsafeHolder.SHORT_BASE + 2, (short)0x3fcd);
        array[122] = (short)0xf22e;
        segment.set(SHORT_UNALIGNED, 1, (short)0x9d53);
        ShortVector.broadcast(ShortVector.SPECIES_64, (short)0xb5cc).intoArray(array, 88);
        ShortVector.broadcast(ShortVector.SPECIES_64, (short)0x3be6).intoArray(array, 0);
        UnsafeHolder.UNSAFE.putShort(array, UnsafeHolder.SHORT_BASE + 126, (short)0xbb40);
        ShortVector.broadcast(ShortVector.SPECIES_64, (short)0x4c64).intoArray(array, 123, VectorMask.fromLong(ShortVector.SPECIES_64, -3L));
        ShortVector.broadcast(ShortVector.SPECIES_64, (short)0x2de8).intoArray(array, 0, new int[] {65, 116, 76, 126}, 0);
        array[3] = (short)0x751c;
        ShortVector.broadcast(ShortVector.SPECIES_64, (short)0xff63).intoArray(array, 0, VectorMask.fromLong(ShortVector.SPECIES_64, -3L));
        ShortVector.broadcast(ShortVector.SPECIES_64, (short)0x2429).intoArray(array, 101, new int[] {1, 5, 22, 10}, 0, VectorMask.fromLong(ShortVector.SPECIES_64, -9L));
        segment.set(SHORT_UNALIGNED, 64, (short)0xccdc);
        return array;
    }

    static short[] referenceMacOrConfirmedShort64NEGATIVErandomMixedStores() {
        short[] array = new short[128];
        MemorySegment segment = MemorySegment.ofArray(array);

        UnsafeHolder.UNSAFE.putShort(array, UnsafeHolder.SHORT_BASE + 2, (short)0x3fcd);
        array[122] = (short)0xf22e;
        segment.set(SHORT_UNALIGNED, 1, (short)0x9d53);
        ShortVector.broadcast(ShortVector.SPECIES_64, (short)0xb5cc).intoArray(array, 88);
        ShortVector.broadcast(ShortVector.SPECIES_64, (short)0x3be6).intoArray(array, 0);
        UnsafeHolder.UNSAFE.putShort(array, UnsafeHolder.SHORT_BASE + 126, (short)0xbb40);
        ShortVector.broadcast(ShortVector.SPECIES_64, (short)0x4c64).intoArray(array, 123, VectorMask.fromLong(ShortVector.SPECIES_64, -3L));
        ShortVector.broadcast(ShortVector.SPECIES_64, (short)0x2de8).intoArray(array, 0, new int[] {65, 116, 76, 126}, 0);
        array[3] = (short)0x751c;
        ShortVector.broadcast(ShortVector.SPECIES_64, (short)0xff63).intoArray(array, 0, VectorMask.fromLong(ShortVector.SPECIES_64, -3L));
        ShortVector.broadcast(ShortVector.SPECIES_64, (short)0x2429).intoArray(array, 101, new int[] {1, 5, 22, 10}, 0, VectorMask.fromLong(ShortVector.SPECIES_64, -9L));
        segment.set(SHORT_UNALIGNED, 64, (short)0xccdc);
        return array;
    }
}

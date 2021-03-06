package org.apache.commons.compress.utils;

import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.charset.Charset;
import java.util.Arrays;

import static org.apache.commons.compress.utils.CharsetNames.UTF_8;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class SeekableInMemoryByteChannelTest {

    private final byte[] testData = "Some data".getBytes(Charset.forName(UTF_8));

    @Test
    public void shouldReadContentsProperly() throws IOException {
        //given
        SeekableInMemoryByteChannel c = new SeekableInMemoryByteChannel(testData);
        ByteBuffer readBuffer = ByteBuffer.allocate(testData.length);
        //when
        int readCount = c.read(readBuffer);
        //then
        assertEquals(testData.length, readCount);
        assertArrayEquals(testData, readBuffer.array());
        assertEquals(testData.length, c.position());
    }

    @Test
    public void shouldReadContentsWhenBiggerBufferSupplied() throws IOException {
        //given
        SeekableInMemoryByteChannel c = new SeekableInMemoryByteChannel(testData);
        ByteBuffer readBuffer = ByteBuffer.allocate(testData.length + 1);
        //when
        int readCount = c.read(readBuffer);
        //then
        assertEquals(testData.length, readCount);
        assertArrayEquals(testData, Arrays.copyOf(readBuffer.array(), testData.length));
        assertEquals(testData.length, c.position());
    }

    @Test
    public void shouldReadDataFromSetPosition() throws IOException {
        //given
        SeekableInMemoryByteChannel c = new SeekableInMemoryByteChannel(testData);
        ByteBuffer readBuffer = ByteBuffer.allocate(4);
        //when
        c.position(5L);
        int readCount = c.read(readBuffer);
        //then
        assertEquals(4L, readCount);
        assertEquals("data", new String(readBuffer.array(), Charset.forName(UTF_8)));
        assertEquals(testData.length, c.position());
    }

    @Test
    public void shouldReadNoDataWhenPositionAtTheEnd() throws IOException {
        //given
        SeekableInMemoryByteChannel c = new SeekableInMemoryByteChannel(testData);
        ByteBuffer readBuffer = ByteBuffer.allocate(testData.length);
        //when
        c.position(testData.length + 1);
        int readCount = c.read(readBuffer);
        //then
        assertEquals(0L, readBuffer.position());
        assertEquals(0, readCount);
    }

    @Test(expected = ClosedChannelException.class)
    public void shouldThrowExceptionOnReadingClosedChannel() throws IOException {
        //given
        SeekableInMemoryByteChannel c = new SeekableInMemoryByteChannel();
        //when
        c.close();
        c.read(ByteBuffer.allocate(1));
    }

    @Test
    public void shouldWriteDataProperly() throws IOException {
        //given
        SeekableInMemoryByteChannel c = new SeekableInMemoryByteChannel();
        ByteBuffer inData = ByteBuffer.wrap(testData);
        //when
        int writeCount = c.write(inData);
        //then
        assertEquals(testData.length, writeCount);
        assertArrayEquals(testData, Arrays.copyOf(c.array(), (int) c.size()));
        assertEquals(testData.length, c.position());
    }

    @Test
    public void shouldWriteDataProperlyAfterPositionSet() throws IOException {
        //given
        SeekableInMemoryByteChannel c = new SeekableInMemoryByteChannel(testData);
        ByteBuffer inData = ByteBuffer.wrap(testData);
        ByteBuffer expectedData = ByteBuffer.allocate(testData.length + 5).put(testData, 0, 5).put(testData);
        //when
        c.position(5L);
        int writeCount = c.write(inData);

        //then
        assertEquals(testData.length, writeCount);
        assertArrayEquals(expectedData.array(), Arrays.copyOf(c.array(), (int) c.size()));
        assertEquals(testData.length + 5, c.position());
    }


    @Test(expected = ClosedChannelException.class)
    public void shouldThrowExceptionOnWritingToClosedChannel() throws IOException {
        //given
        SeekableInMemoryByteChannel c = new SeekableInMemoryByteChannel();
        //when
        c.close();
        c.write(ByteBuffer.allocate(1));
    }

    @Test
    public void shouldTruncateContentsProperly() throws IOException {
        //given
        SeekableInMemoryByteChannel c = new SeekableInMemoryByteChannel(testData);
        //when
        c.truncate(4);
        //then
        byte[] bytes = Arrays.copyOf(c.array(), (int) c.size());
        assertEquals("Some", new String(bytes, Charset.forName(UTF_8)));
    }

    @Test
    public void shouldSetProperPositionOnTruncate() throws IOException {
        //given
        SeekableInMemoryByteChannel c = new SeekableInMemoryByteChannel(testData);
        //when
        c.position(testData.length);
        c.truncate(4L);
        //then
        assertEquals(4L, c.position());
        assertEquals(4L, c.size());
    }

    @Test
    public void shouldSetProperPosition() throws IOException {
        //given
        SeekableInMemoryByteChannel c = new SeekableInMemoryByteChannel(testData);
        //when
        long posAtFour = c.position(4L).position();
        long posAtTheEnd = c.position(testData.length).position();
        long posPastTheEnd = c.position(testData.length + 1L).position();
        //then
        assertEquals(4L, posAtFour);
        assertEquals(c.size(), posAtTheEnd);
        assertEquals(posPastTheEnd, posPastTheEnd);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenSettingIncorrectPosition() throws IOException {
        //given
        SeekableInMemoryByteChannel c = new SeekableInMemoryByteChannel();
        //when
        c.position(Integer.MAX_VALUE + 1L);
    }

    @Test(expected = ClosedChannelException.class)
    public void shouldThrowExceptionWhenSettingPositionOnClosedChannel() throws IOException {
        //given
        SeekableInMemoryByteChannel c = new SeekableInMemoryByteChannel();
        //when
        c.close();
        c.position(1L);
    }

}

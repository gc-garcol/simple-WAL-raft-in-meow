package gc.garcol.journal.core;

import gc.garcol.journal.core.global.Env;
import gc.garcol.raft.proto.LogProto;

import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class LogRepository implements Closeable
{
    private final LogMetadata metadata;
    private RandomAccessFile logFile;
    private RandomAccessFile logIndexFile;
    private FileChannel logChannel;
    private FileChannel logIndexChannel;

    public LogRepository()
    {
        try
        {
            metadata = readMetadata();
            openFiles(metadata.lastTerm);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public LogProto.LogEntry readLog(long term, long index) throws IOException
    {
        LogIndex logIndex = getIndex(index);
        ByteBuffer buffer = ByteBuffer.allocate((int)logIndex.length());
        logChannel.read(buffer, logIndex.offset());
        buffer.flip();
        return LogProto.LogEntry.parseFrom(buffer.array());
    }

    public void writeLog(LogProto.LogEntry logEntry) throws IOException
    {
        long offset = logChannel.size();
        byte[] data = logEntry.toByteArray();
        ByteBuffer buffer = ByteBuffer.wrap(data);
        logChannel.write(buffer, offset);
        setIndex(offset, data.length);
        if (offset + data.length > Env.MAX_RECORDS_PER_TERM)
        {
            metadata.lastTerm++;
            persistMetadata();
            closeCurrentFiles();
            openFiles(metadata.lastTerm);
        }
    }

    private LogMetadata persistMetadata() throws IOException
    {
        try (RandomAccessFile metadataFile = new RandomAccessFile(Env.METADATA_FILE, "rw"))
        {
            metadataFile.seek(0);
            metadataFile.writeLong(metadata.lastTerm);
            return metadata;
        }
    }

    private LogMetadata readMetadata() throws IOException
    {
        var logMetadata = new LogMetadata();
        try (RandomAccessFile metadataFile = new RandomAccessFile(Env.METADATA_FILE, "r"))
        {
            metadataFile.seek(0);
            logMetadata.lastTerm = metadataFile.readLong();
        }
        catch (Exception e)
        {
            logMetadata.lastTerm = 1;
        }
        return logMetadata;
    }

    private void setIndex(long offset, long length) throws IOException
    {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer
            .putLong(offset)
            .putLong(length);
        buffer.flip();
        logIndexChannel.write(buffer, logIndexChannel.size());
    }

    private LogIndex getIndex(long index) throws IOException
    {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        logIndexChannel.read(buffer, index * 16);
        buffer.flip();
        long offset = buffer.getLong();
        long length = buffer.getLong();
        return new LogIndex(offset, length);
    }

    @Override
    public void close() throws IOException
    {
        closeCurrentFiles();
    }

    private void openFiles(long term) throws IOException
    {
        logFile = new RandomAccessFile(Env.LOG_DIR + "/" + LogUtil.logName(term), "rw");
        logIndexFile = new RandomAccessFile(Env.LOG_DIR + "/" + LogUtil.indexName(term), "rw");
        logChannel = logFile.getChannel();
        logIndexChannel = logIndexFile.getChannel();
    }

    private void closeCurrentFiles() throws IOException
    {
        if (logChannel != null)
        {
            logChannel.close();
        }
        if (logIndexChannel != null)
        {
            logIndexChannel.close();
        }
        if (logFile != null)
        {
            logFile.close();
        }
        if (logIndexFile != null)
        {
            logIndexFile.close();
        }
    }
}

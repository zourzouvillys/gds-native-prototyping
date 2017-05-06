package net.smacke.jaydio;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import net.smacke.jaydio.buffer.AlignedDirectByteBuffer;
import net.smacke.jaydio.channel.DirectIoByteChannel;

public class DirectIoLibTest {

	@Test
	public void test() throws IOException {

		final DirectIoByteChannel channel = DirectIoByteChannel.getChannel(new File("/tmp/gds"), false);

		channel.allocate(0, 0, 65535);

		channel.seek(1024, 0);

		final AlignedDirectByteBuffer buf = AlignedDirectByteBuffer.allocate(channel.getLib(), 8192);


		for (int i = 0 ; i < 8192 ; ++i) {
			buf.put((byte)(i & 255));
		}

		buf.position(4096);

		channel.write(buf, 4096);

		channel.sync(1024, 8);
		channel.seek(0, 0);

	}

}

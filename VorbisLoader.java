//LWJGL 3 comes with a utility for loading Vorbis files.
import static org.lwjgl.stb.STBVorbis.*;

//However, that same utility has to use a C library to clean up.
import static org.lwjgl.system.libc.LibCStdlib.free;

//And here is our audio API itself, the one, the only, OpenAL!
import static org.lwjgl.openal.AL10.*;

//And here is some memory management imports.
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import java.nio.ShortBuffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.io.IOException;

//And this is just to make my life easier.
import org.apache.commons.io.IOUtils;

public class VorbisLoader {
	public static int loadFromResource(String resName) {
		try(MemoryStack stack = MemoryStack.stackPush()){
      //Stack allocate audio properties because they're small bits of data.
			IntBuffer channels = stack.mallocInt(1);
			IntBuffer sampleRate = stack.mallocInt(1);
			
      //STBVorbis can't read Java Resources, so we have to read the file to it.
			byte[] resourceContents = IOUtils.resourceToByteArray(resName);
			ByteBuffer resourceBuffer = MemoryUtil.memAlloc(resourceContents.length); //We allocate this buffer on the heap because the audio file itself could be bigger than the 64k stack.
			resourceBuffer.put(resourceContents);
			resourceBuffer.flip();
			
      //STBVorbis is here to decode the .ogg file.
			ShortBuffer rawAudio = stb_vorbis_decode_memory(resourceBuffer, channels, sampleRate);
			
      //We don't need the vorbis file in memory anymore, as we have the raw audio data now, so let's clean up as we go.
			MemoryUtil.memFree(resourceBuffer);
			
      //I think this code speaks for itself.
			int format = -1;
			int numOfChannels = channels.get(0);
			if(numOfChannels == 1)
				format = AL_FORMAT_MONO16;
			else if(numOfChannels == 2)
				format = AL_FORMAT_STEREO16;
			
      //Now we'll put the audio data into an OpenAL Buffer.
			int alBuffer = alGenBuffers();
			alBufferData(alBuffer, format, rawAudio, sampleRate.get(0));
			
      //Now that OpenAL has the data, we don't need it anymore. Bye-bye.
			free(rawAudio);
			
      //And this exits the function. Use your audio as you please!
			return alBuffer;
		} 
		catch(IOException e) {
			e.printStackTrace();
			return 0;
		}
	}
}

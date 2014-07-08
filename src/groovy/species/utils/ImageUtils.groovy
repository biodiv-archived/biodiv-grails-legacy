package species.utils

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import org.imgscalr.*;
import java.awt.color.CMMException;
import java.util.HashMap;
import javax.imageio.IIOException;
import net.sf.jmimemagic.Magic;

import org.apache.commons.logging.LogFactory;

class ImageUtils {

	private static final log = LogFactory.getLog(this);


	//TODO: chk synchronization probs ... static blocks
	/**
	 * Creates scaled versions of given image in the directory.
	 * Converts image to jpg 
	 * Uses suffixes as defined in Config.
	 * @param imageFile
	 * @param dir
	 */
	static void createScaledImages(  File imageFile, File dir) {
		log.debug "Creating scaled versions of image : "+imageFile.getAbsolutePath();

		def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.resources.images

		String fileName = imageFile.getName();
		int lastIndex = fileName.lastIndexOf('.');
		String name = fileName;
        String originalFileExt = ".jpg";
 		if(lastIndex != -1) {
			name = fileName.substring(0, lastIndex);
            originalFileExt = fileName.substring(lastIndex, fileName.length())
		}
        //Auto orienting to fix orientation for galleria and saving with "_o" 
        //and using this oriented image for futher creation 
        File imageFile1= ImageUtils.autoOrientOriginalImage(imageFile, new File(dir, name+"_o"+originalFileExt))

        ///////////////////////////////////////////////////

        log.debug "Creating gallery image";
        def extension = config.gallery.suffix
        ImageUtils.convert(imageFile1, new File(dir, name+extension), config.gallery.width, config.gallery.height, 100);

        ///////////////////////////////////////////////////
        log.debug "Creating gallery thumbnail image";
        extension = config.galleryThumbnail.suffix
        ImageUtils.convert(imageFile1, new File(dir, name+extension), config.galleryThumbnail.width, config.galleryThumbnail.height, 100);

        ////////////////////////////////////////////////
        log.debug "Creating thumbnail image";
        extension = config.thumbnail.suffix 
        try{
            doResize(imageFile1, new File(dir, name+extension), config.thumbnail.width, config.thumbnail.height);
        } catch (Exception e) {
            log.debug "Error while resizing image probably due to unsupported type so using _gall_th.jpg image for _th1.jpg image $imageFile"
            def jpgGall_extension = config.gallery.suffix

            try{
                doResize(new File(dir, name+jpgGall_extension), new File(dir, name+extension), config.thumbnail.width, config.thumbnail.height);
            }
            catch (Exception e1){
                log.error "Error even on using _gall_th.jpg image for this file " + dir +"/" + name+jpgGall_extension +" target file " + name+extension;
                e1.printStackTrace()
            }

        } 
				
	}

	/**
	 * Uses a Runtime.exec()to use imagemagick to perform the given conversion
	 * operation. Returns true on success, false on failure. Does not check if
	 * either file exists.
	 *
	 * @param in Description of the Parameter
	 * @param out Description of the Parameter
	 * @param newSize Description of the Parameter
	 * @param quality Description of the Parameter
	 * @return Description of the Return Value
	 */
	private static boolean convert(File inImg, File outImg, int width, int height, int quality) {

		if (!quality || quality < 0 || quality > 100) {
			quality = 75;
		}

		ArrayList command = new ArrayList(10);

		// note: CONVERT_PROG is a class variable that stores the location of ImageMagick's convert command
		// it might be something like "/usr/local/magick/bin/convert" or something else, depending on where you installed it.
		def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
		command.add(config.imageConverterProg);
        command.add("-auto-orient")
		command.add("-resize");
		command.add(width + "x" + height);
		command.add("-quality");
		command.add("" + quality);
		command.add(inImg.getAbsolutePath());
		command.add(outImg.getAbsolutePath());

		log.debug command;

		def proc = command.execute()                 // Call *execute* on the string
		proc.waitFor()                               // Wait for the command to finish

		// Obtain status and output
		log.debug "return code: ${ proc.exitValue()}"
		log.debug "stderr: ${proc.err.text}"
		log.debug "stdout: ${proc.in.text}" // *out* from the external program is *in* for groovy

		if(proc.exitValue() == 0) {
			jpegOptimize(outImg);
		}
		return (proc.exitValue() == 0)
	}
    
    private static File autoOrientOriginalImage(File inImg, File outImg) {
        
		ArrayList command = new ArrayList(6);

		def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
		command.add(config.imageConverterProg);
        command.add("-auto-orient")
		command.add(inImg.getAbsolutePath());
		command.add(outImg.getAbsolutePath());

		log.debug command;

		def proc = command.execute()                 // Call *execute* on the string
		proc.waitFor()                               // Wait for the command to finish

		// Obtain status and output
		log.debug "return code: ${ proc.exitValue()}"
		log.debug "stderr: ${proc.err.text}"
		log.debug "stdout: ${proc.in.text}" // *out* from the external program is *in* for groovy

		return outImg 
    }

	/**
	 *Resizing Image to 200*200
	 */
    public static void doResize(File inImg, File outImg, int width, int height) throws Exception{
        String fileName = inImg.getAbsolutePath();
		//System.out.println(fileName);
		String ext = checkMIME(inImg);
        BufferedImage im = null;
        try{       
            im = ImageIO.read(inImg);
        }catch(IIOException e){
            try{
                im = JpegReader.readCMYKImage(inImg);
            }catch(Exception my_e){
                log.error "CMYK Image also couldnt be read";
            }
        }
        doResize(im, outImg, width, height, ext);
    }
	//XXX change this method to private after running migration script
    private static void doResize(BufferedImage im, File outImg, int width, int height,String ext) throws Exception{
	   //if(inImg != null){
        //String fileName = outImg.getAbsolutePath();
		//System.out.println(fileName);
		//String ext = fileName.tokenize('.').last();
        //ext = ext.toLowerCase();
        //String ext = "jpg";
		
		BufferedImage scaled = null;
		BufferedImage cropped = null;

		int img_width = im.getWidth();
		int img_height = im.getHeight();
		float img_ratio = (img_width) / (float) (img_height);
		//System.out.println(img_ratio);
		// Case 1: When Width greater than height of image.
 		if (img_width > img_height) {
			int new_width = (int) (height * img_ratio);
			scaled = Scalr.resize(im, Scalr.Method.AUTOMATIC, new_width, height);
			int sca_height = scaled.getHeight();
			int x = (new_width - sca_height) / 2;
			int y = 0;
			int rect_width = sca_height;
			int rect_height = sca_height;
			cropped = scaled.getSubimage(x, y, rect_width, rect_height);
		 }
		// Case 2: When height greater than width of image.
 		else {
			int new_height = (int) (width /img_ratio);
			scaled = Scalr.resize(im, Scalr.Method.AUTOMATIC, width, new_height);
			int sca_width = scaled.getWidth();
			int x = 0;
			int y = (new_height - sca_width) / 2;
			int rect_width = sca_width;
			int rect_height = sca_width;
			cropped = scaled.getSubimage(x, y, rect_width, rect_height);
		} 
		ImageIO.write(cropped, ext, outImg);
        jpegOptimize(outImg);

		//		} catch(Exception e){
		//
		//			//System.out.println(e.getMessage());
		//		}
        //}
	}

    private static String checkMIME(File inImg) {
		String mimeType = "";
		try {
			mimeType = Magic.getMagicMatch(inImg, false).getMimeType();
		} catch (Exception e){
            //e.printStackTrace();
            log.error "MIME TYPE NOT FOUND so making it as image/jpg"
            mimeType = "image/jpg";
		
        } 
       		mimeType = mimeType.split("/")[1];
			return mimeType;
		
	}

	/**
	 * Uses a Runtime.exec()to use jpegoptim program to optimize 
	 * size of jpg files by stripping off all meta data
	 *
	 * @param file 
	 * @return Description of the Return Value
	 */
	private static boolean jpegOptimize(File file) {

		ArrayList command = new ArrayList(10);

		def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
		command.add(config.jpegOptimProg);
		//command.add("--strip-all");// we are reading location tags so commenting for now
		command.add(file.getAbsolutePath());

		log.debug command;

		def proc = command.execute()                 // Call *execute* on the string
		proc.waitFor()                               // Wait for the command to finish

		log.debug "return code: ${ proc.exitValue()}"
		log.debug "stderr: ${proc.err.text}"
		log.debug "stdout: ${proc.in.text}" // *out* from the external program is *in* for groovy

		return (proc.exitValue() == 0)
	}


	/**
	 * Convenience method that returns a scaled instance of the
	 * provided {@code BufferedImage}.
	 *
	 * @param img the original image to be scaled
	 * @param targetWidth the desired width of the scaled instance,
	 *    in pixels
	 * @param targetHeight the desired height of the scaled instance,
	 *    in pixels
	 * @param hint one of the rendering hints that corresponds to
	 *    {@code RenderingHints.KEY_INTERPOLATION} (e.g.
	 *    {@code RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR},
	 *    {@code RenderingHints.VALUE_INTERPOLATION_BILINEAR},
	 *    {@code RenderingHints.VALUE_INTERPOLATION_BICUBIC})
	 * @param higherQuality if true, this method will use a multi-step
	 *    scaling technique that provides higher quality than the usual
	 *    one-step technique (only useful in downscaling cases, where
	 *    {@code targetWidth} or {@code targetHeight} is
	 *    smaller than the original dimensions, and generally only when
	 *    the {@code BILINEAR} hint is specified)
	 * @return a scaled version of the original {@code BufferedImage}
	 */
	static BufferedImage getScaledInstance(BufferedImage img,
	int targetWidth,
	int targetHeight,
	boolean higherQuality) {

		int type = (img.getTransparency() == Transparency.OPAQUE) ?
				BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
		BufferedImage ret = (BufferedImage)img;
		int w, h;
		if (higherQuality) {
			// Use multi-step technique: start with original size, then
			// scale down in multiple passes with drawImage()
			// until the target size is reached
			w = img.getWidth();
			h = img.getHeight();
		} else {
			// Use one-step technique: scale directly from original
			// size to target size with a single drawImage() call
			w = targetWidth;
			h = targetHeight;
		}

		while(true) {
			if (higherQuality && w > targetWidth) {
				w /= 2;
				if (w < targetWidth) {
					w = targetWidth;
				}
			}

			if (higherQuality && h > targetHeight) {
				h /= 2;
				if (h < targetHeight) {
					h = targetHeight;
				}
			}

			BufferedImage tmp = new BufferedImage(w, h, type);
			Graphics2D g2 = tmp.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2.drawImage(ret, 0, 0, w, h, null);
			g2.dispose();

			ret = tmp;

			if (w != targetWidth || h != targetHeight) {
				continue;
			} else {
				break;
			}
		}

		return ret;
	}

	static String getFileName(String name, ImageType type, String defaultFileType=null) {
		if(!name) return;

		if(!type) type = ImageType.NORMAL;

		def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config

		if(!defaultFileType) defaultFileType = '.'+config.speciesPortal.resources.images.defaultType;

        String ext = Utils.getCleanFileExtension(name);

		switch(type) {
			case ImageType.NORMAL :
				if(ext) {
					//if filename already has an extention
					name = name?.replaceFirst(/\.[a-zA-Z]+$/, ImageType.NORMAL.getSuffix()).replaceFirst('.'+config.speciesPortal.resources.images.defaultType, defaultFileType);
				} else {
					name = name?.plus(ImageType.NORMAL.getSuffix()).replaceFirst('.'+config.speciesPortal.resources.images.defaultType, defaultFileType);
				}
				break;
			case ImageType.SMALL :
				if(ext) {
					//if filename alreadyy has an extention
					name = name?.replaceFirst(/\.[a-zA-Z]+$/, ImageType.SMALL.getSuffix()).replaceFirst('.'+config.speciesPortal.resources.images.defaultType, defaultFileType);
				} else {
					name = name?.plus(ImageType.SMALL.getSuffix()).replaceFirst('.'+config.speciesPortal.resources.images.defaultType, defaultFileType);
				}
				break;
			case ImageType.VERY_SMALL :
				if(ext) {
					//if filename already has an extention
					name = name?.replaceFirst(/\.[a-zA-Z]+$/, ImageType.VERY_SMALL.getSuffix()).replaceFirst('.'+config.speciesPortal.resources.images.defaultType, defaultFileType);
				} else {
					name = name?.plus(ImageType.VERY_SMALL.getSuffix()).replaceFirst('.'+config.speciesPortal.resources.images.defaultType, defaultFileType);
				}
				break;
			case ImageType.LARGE :
				if(ext) {
					//if filename already has an extention
					name = name?.replaceFirst(/\.[a-zA-Z]+$/, ImageType.LARGE.getSuffix()).replaceFirst('.'+config.speciesPortal.resources.images.defaultType, defaultFileType);
				} else {
					name = name?.plus(ImageType.LARGE.getSuffix()).replaceFirst('.'+config.speciesPortal.resources.images.defaultType, defaultFileType);
				}
				break;
			case ImageType.ORIGINAL :
			default:
				name = name + defaultFileType;

		}
		return name;
	}

}


public enum ImageType {
	ORIGINAL, NORMAL,SMALL,VERY_SMALL, LARGE
	def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config

	public String getSuffix() {
		switch(this) {
			case ORIGINAL : return ""
			case NORMAL : return config.speciesPortal.resources.images.thumbnail.suffix
			case SMALL : return config.speciesPortal.resources.images.galleryThumbnail.suffix
			case VERY_SMALL : return '_32X32'+'.'+config.speciesPortal.resources.images.defaultType
			case LARGE : return config.speciesPortal.resources.images.gallery.suffix
		}
	}
}


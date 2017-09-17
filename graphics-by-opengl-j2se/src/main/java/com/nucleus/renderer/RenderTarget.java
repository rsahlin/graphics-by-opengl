package com.nucleus.renderer;

import com.nucleus.opengl.GLESWrapper.GLES20;

/**
 * Represents a render target, currently only supports window framebuffer
 * Future versions will add support for changing render target.
 */
public class RenderTarget {

	public enum Target {
		FRAMEBUFFER(GLES20.GL_FRAMEBUFFER),
		RENDERBUFFER(GLES20.GL_RENDERBUFFER);

		public final int target;
		
		private Target(int target) {
			this.target = target;
		}
		
		
	}

	private Target target = Target.FRAMEBUFFER;

	public Target getTarget() {
		return target;
	}
	
}

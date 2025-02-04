/*
* This is the source code of Telegram for Android v. 5.x.x.
* It is licensed under GNU GPL v. 2 or later.
* You should have received a copy of the license in this archive (see LICENSE).
*
* Copyright Irsyad DEV, 2022-2023.
*/
package org.bytedata.manager.ui.components;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.ObjectKey;

import java.io.File;

public class BackupImageView extends androidx.appcompat.widget.AppCompatImageView {
	
	protected static final String ASSETS_RESOURCE = "file:///android_asset/";
	
	protected int width = -1;
	protected int height = -1;
	private int roundRadius;
	
	private RequestOptions sharedOptions;
	private RequestBuilder<Drawable> glideRequest;
	private RequestListener<Drawable> requestListener;
	private boolean allowLoadingOnAttachedOnly = false;
	private boolean needOverride = false;
	
	private boolean needPinchToZoom;
	private boolean circleCrop;
	private boolean loadFailed;
	private boolean attached;
	
	private float thumbalSize = -1f;
	private RequestDelegate delegate;
	
	//private Shimmer shimmer;
	//private ShimmerDrawable shimmerDrawable;
	
	public interface RequestDelegate {
		public void onLoadFailed();
		public void onResourceReady();
	}
	
	public BackupImageView(@NonNull Context context) {
		this(context, null, 0);
	}
	
	public BackupImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public BackupImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}
	
	private void init() {
		sharedOptions = new RequestOptions()
		.diskCacheStrategy(DiskCacheStrategy.ALL)
		.timeout(10000);
		
		setCircleCrop(circleCrop);
		
		requestListener = new RequestListener<Drawable>() {
			@Override
			public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
				loadFailed = true;
				if (delegate != null) {
					delegate.onLoadFailed();
				}
				return false;
			}
			
			@Override
			public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
				loadFailed = false;
				if (delegate != null) {
					delegate.onResourceReady();
				}
				return false;
			}
		};
	}
	
	public void setRequestListener(RequestDelegate req) {
		this.delegate = req;
	}
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		attached = false;
		if (allowLoadingOnAttachedOnly) {
			cancelLoadImage();
		}
		if (needPinchToZoom) {
			
		}
	}
	
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		attached = true;
		if (needPinchToZoom) {
			
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setSize(getMeasuredWidth(), getMeasuredHeight());
	}
	
	@Override
	public void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		final int width = getWidth();
		final int height = getHeight();
		setSize(width, height);
	}
	
	@NonNull
	public RequestOptions getRequestOptions() {
		return sharedOptions;
	}
	
	@Nullable
	public Drawable getStaticThumb() {
		return getDrawable();
	}
	
	public int getRoundRadius() {
		return roundRadius;
	}
	
	public boolean isCircleCrop() {
		return circleCrop;
	}
	
	public void setSize(int w, int h) {
		if (needOverride || (width == w && height == h) || w == 0 || h == 0) {
			return;
		}
		width = w;
		height = h;
		sharedOptions.override(width, height);
	}
	
	public void override(int w, int h) {
		override(true, w, h);
	}
	
	public void override(boolean override, int w, int h) {
		if ((needOverride == override) && (width == w && height == h) || w == 0 || h == 0) {
			return;
		}
		needOverride = override;
		width = w;
		height = h;
		sharedOptions.override(width, height);
	}
	
	public void setCustomSignature(Object signature) {
		if (signature == null) {
			throw new IllegalArgumentException("Signature cannot be null");
		}
        sharedOptions.signature(new ObjectKey(signature));
	}
    
    public void setImage(@NonNull Uri uri, @NonNull Object signature) {
        setCustomSignature(signature);
        setImage(uri);
    }
	
	public void setPriorityHigh(boolean high) {
		sharedOptions.priority(high ? Priority.HIGH : Priority.NORMAL);
	}
	
	public void setThumnailSize(float size) {
		thumbalSize = size;
	}
	
	public void setTimeout(int timeout) {
		sharedOptions.timeout(timeout);
	}
	
	public void setRoundRadius(int rad) {
		roundRadius = rad;
		sharedOptions.transform(new CenterCrop(), new RoundedCorners(roundRadius));
	}
	
	/*public void setBlur(int size) {
		sharedOptions.transform(new MultiTransformation<>(new BlurTransformation((int) size), new CenterCrop()));
	}*/
	
	public void setCircleCrop(boolean value) {
		circleCrop = value;
		if (value) {
			sharedOptions.circleCrop();
		} else {
			sharedOptions.transform(new CenterCrop());
		}
	}
    
    public void setInsideTransform() {
        sharedOptions.centerInsideTransform();
    }
    
    public void setAspectFit(boolean value) {
		if (value) {
			sharedOptions.fitCenter();
		}
	}
	
	public void setAllowLoadingOnAttachedOnly(boolean allow) {
		allowLoadingOnAttachedOnly = allow;
	}
	
	public void needThumnail(float thumb) {
		thumbalSize = thumb;
	}
	
	public void setPlaceHolder(@DrawableRes int resId) {
		sharedOptions.placeholder(resId);
	}
	
	/*public void setPlaceHolderShimmer(boolean enable) {
		if (!enable) {
			shimmerDrawable = null;
			return;
		}
		if (shimmerDrawable != null) {
            return;
        }
		if (shimmer == null) {
		    shimmer = new Shimmer.ColorHighlightBuilder()
		    .setDuration(2800)
		    .setBaseAlpha(1.0f)
		    .setHighlightAlpha(0.09f)
		    .setDirection(Shimmer.Direction.LEFT_TO_RIGHT)
            .setBaseColor(Theme.getColor(Theme.key_divider))
            .setHighlightColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText))
		    .setAutoStart(true)
		    .build();
		}
		shimmerDrawable = new ShimmerDrawable();
		shimmerDrawable.setShimmer(shimmer);
	}*/
	
	public void setError(@DrawableRes int resId) {
		sharedOptions.error(resId);
	}
	
	public void setPlaceHolder(Drawable drawable) {
		sharedOptions.placeholder(drawable);
	}
	
	public void setError(Drawable drawable) {
		sharedOptions.error(drawable);
	}
	
	public void setImage(@NonNull Drawable drawable) {
		glideRequest = Glide.with(getContext())
		.load(drawable)
		.apply(sharedOptions)
		.dontAnimate()
		.transition(DrawableTransitionOptions.withCrossFade())
		.listener(requestListener);
		if (thumbalSize != -1f) {
			glideRequest.thumbnail(thumbalSize);
		}
		glideRequest.into(this);
	}
	
	public void setImage(@DrawableRes int resId) {
		glideRequest = Glide.with(getContext())
		.load(resId)
		.apply(sharedOptions)
		.dontAnimate()
		.transition(DrawableTransitionOptions.withCrossFade())
		.listener(requestListener);
		if (thumbalSize != -1f) {
			glideRequest.thumbnail(thumbalSize);
		}
		glideRequest.into(this);
	}
	
	public void setImage(@NonNull String url) {
		if (TextUtils.isEmpty(url)) {
			return;
		}
		setImage(Uri.parse(url));
	}
	
	public void setImageFromAssets(@NonNull String imageFileName) {
		if (TextUtils.isEmpty(imageFileName)) {
			return;
		}
		setImage(Uri.parse(ASSETS_RESOURCE + imageFileName));
	}
	
	public void setImage(String uri, int rad) {
		setRoundRadius(rad);
		setImage(uri);
	}
	
	public void setImage(@NonNull Uri uri) {
		glideRequest = Glide.with(getContext())
		.load(uri)
		.apply(sharedOptions)
		.dontAnimate()
		.transition(DrawableTransitionOptions.withCrossFade())
		.listener(requestListener);
		if (thumbalSize != -1f) {
			glideRequest.thumbnail(thumbalSize);
		}
        /*if (shimmerDrawable != null) {
            glideRequest.placeholder(shimmerDrawable);
        }*/
		glideRequest.into(this);
	}
	
	public void setImage(String url, String thumbUrl) {
		if (TextUtils.isEmpty(url) || TextUtils.isEmpty(thumbUrl)) {
			return;
		}
		setImage(Uri.parse(url), Uri.parse(thumbUrl));
	}
	
	public void setImage(@NonNull Uri uri, @NonNull Uri thumbUri) {
		glideRequest = Glide.with(getContext())
		.load(uri)
		.apply(sharedOptions)
		.dontAnimate()
		.transition(DrawableTransitionOptions.withCrossFade())
		.listener(requestListener)
		.thumbnail(
		    Glide.with(getContext())
		    .load(thumbUri)
		    .apply(sharedOptions)
		    .dontAnimate()
		    .transition(DrawableTransitionOptions.withCrossFade())
		);
        /*if (shimmerDrawable != null) {
            glideRequest.placeholder(shimmerDrawable);
        }*/
		glideRequest.into(this);
	}
	
	public void setImageNormalSize(@NonNull String url) {
		glideRequest = Glide.with(getContext())
		.load(url)
		.dontAnimate()
		.transition(DrawableTransitionOptions.withCrossFade())
		.listener(requestListener);
        /*if (shimmerDrawable != null) {
            glideRequest.placeholder(shimmerDrawable);
        }*/
		glideRequest.into(this);
	}
    
    public void setImage(@NonNull File uri) {
		glideRequest = Glide.with(getContext())
		.load(uri)
		.apply(sharedOptions)
		.dontAnimate()
		.transition(DrawableTransitionOptions.withCrossFade())
		.listener(requestListener);
        /*if (shimmerDrawable != null) {
            glideRequest.placeholder(shimmerDrawable);
        }*/
		glideRequest.into(this);
	}
	
	public void clearImage() {
		Glide.with(getContext()).clear(this);
		glideRequest = null;
	}
	
	public void cancelLoadImage() {
		if (glideRequest != null) {
			clearImage();
		}
	}
	
	public void setLayerNum(int value) {
		setRoundRadius(value);
	}
}

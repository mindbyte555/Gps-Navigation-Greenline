package com.example.gpstest.AdsManager

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.gpstest.R
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

class TemplateView : FrameLayout {
    private var templateType = 0
    private var styles: NativeTemplateStyle? = null
    private var nativeAd: NativeAd? = null
    var nativeAdView: NativeAdView? = null
        private set
    private var primaryView: TextView? = null
    private var secondaryView: TextView? = null
    private var ratingBar: TextView? = null
    private var ratingBarimg: ImageView? = null
    private var tertiaryView: TextView? = null
    private var adPrice: TextView? = null
    private var iconView: ImageView? = null
    private var mediaView: MediaView? = null
    private var callToActionView: Button? = null
    private var background: ConstraintLayout? = null

    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView(context, attrs)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        initView(context, attrs)
    }

    fun setStyles(styles: NativeTemplateStyle?) {
        this.styles = styles
        applyStyles()
    }

    private fun applyStyles() {
        val mainBackground: Drawable? = styles!!.getMainBackgroundColor()
        if (mainBackground != null) {
            background!!.background = mainBackground
            if (primaryView != null) {
                primaryView!!.background = mainBackground
            }
            if (secondaryView != null) {
                secondaryView!!.background = mainBackground
            }
            if (tertiaryView != null) {
                tertiaryView!!.background = mainBackground
            }
        }
        val primary = styles!!.getPrimaryTextTypeface()
        if (primary != null && primaryView != null) {
            primaryView!!.typeface = primary
        }
        val secondary = styles!!.getSecondaryTextTypeface()
        if (secondary != null && secondaryView != null) {
            secondaryView!!.typeface = secondary
        }
        val tertiary = styles!!.getTertiaryTextTypeface()
        if (tertiary != null && tertiaryView != null) {
            tertiaryView!!.typeface = tertiary
        }
        val ctaTypeface = styles!!.getCallToActionTextTypeface()
        if (ctaTypeface != null && callToActionView != null) {
            callToActionView!!.typeface = ctaTypeface
        }
        val primaryTypefaceColor = styles!!.getPrimaryTextTypefaceColor()
        if (primaryTypefaceColor > 0 && primaryView != null) {
            primaryView!!.setTextColor(primaryTypefaceColor)
        }
        val secondaryTypefaceColor = styles!!.getSecondaryTextTypefaceColor()
        if (secondaryTypefaceColor > 0 && secondaryView != null) {
            secondaryView!!.setTextColor(secondaryTypefaceColor)
        }
        val tertiaryTypefaceColor = styles!!.getTertiaryTextTypefaceColor()
        if (tertiaryTypefaceColor > 0 && tertiaryView != null) {
            tertiaryView!!.setTextColor(tertiaryTypefaceColor)
        }
        val ctaTypefaceColor = styles!!.getCallToActionTypefaceColor()
        if (ctaTypefaceColor > 0 && callToActionView != null) {
            callToActionView!!.setTextColor(ctaTypefaceColor)
        }
        val ctaTextSize = styles!!.getCallToActionTextSize()
        if (ctaTextSize > 0 && callToActionView != null) {
            callToActionView!!.textSize = ctaTextSize
        }
        val primaryTextSize = styles!!.getPrimaryTextSize()
        if (primaryTextSize > 0 && primaryView != null) {
            primaryView!!.textSize = primaryTextSize
        }
        val secondaryTextSize = styles!!.getSecondaryTextSize()
        if (secondaryTextSize > 0 && secondaryView != null) {
            secondaryView!!.textSize = secondaryTextSize
        }
        val tertiaryTextSize = styles!!.getTertiaryTextSize()
        if (tertiaryTextSize > 0 && tertiaryView != null) {
            tertiaryView!!.textSize = tertiaryTextSize
        }
        val ctaBackground: Drawable? = styles!!.getCallToActionBackgroundColor()
        if (ctaBackground != null && callToActionView != null) {
            callToActionView!!.background = ctaBackground
        }
        val primaryBackground: Drawable? = styles!!.getPrimaryTextBackgroundColor()
        if (primaryBackground != null && primaryView != null) {
            primaryView!!.background = primaryBackground
        }
        val secondaryBackground: Drawable? = styles!!.getSecondaryTextBackgroundColor()
        if (secondaryBackground != null && secondaryView != null) {
            secondaryView!!.background = secondaryBackground
        }
        val tertiaryBackground: Drawable? = styles!!.getTertiaryTextBackgroundColor()
        if (tertiaryBackground != null && tertiaryView != null) {
            tertiaryView!!.background = tertiaryBackground
        }
        invalidate()
        requestLayout()
    }

    private fun adHasOnlyStore(nativeAd: NativeAd): Boolean {
        val store = nativeAd.store
        val advertiser = nativeAd.advertiser
        return !TextUtils.isEmpty(store) && TextUtils.isEmpty(advertiser)
    }

    fun setNativeAd(nativeAd: NativeAd) {
        this.nativeAd = nativeAd
        val store = nativeAd.store
        val advertiser = nativeAd.advertiser
        val headline = nativeAd.headline
        val price = nativeAd.price
        val body = nativeAd.body
        val cta = nativeAd.callToAction
        val starRating = nativeAd.starRating
        val icon = nativeAd.icon
        val secondaryText: String
        nativeAdView!!.callToActionView = callToActionView
        nativeAdView!!.headlineView = primaryView
        nativeAdView!!.mediaView = mediaView
        secondaryView!!.visibility = VISIBLE
        if (adHasOnlyStore(nativeAd)) {
            nativeAdView!!.storeView = secondaryView
            secondaryText = store.toString()
        } else if (!TextUtils.isEmpty(advertiser)) {
            nativeAdView!!.advertiserView = secondaryView
            secondaryText = advertiser.toString()
        } else {
            secondaryText = ""
        }
        primaryView!!.text = headline
        adPrice?.text = price
        callToActionView!!.text = cta

        //  Set the secondary view to be the star rating if available.
        if (starRating != null && starRating > 0) {
            secondaryView!!.visibility = GONE
            ratingBar!!.visibility = VISIBLE
            ratingBarimg!!.visibility = VISIBLE
            ratingBar!!.text = starRating.toFloat().toString()
            nativeAdView!!.starRatingView = ratingBar
        } else {
            secondaryView!!.text = secondaryText
            secondaryView!!.visibility = VISIBLE
            ratingBar!!.visibility = GONE
            ratingBarimg?.visibility = GONE
        }
        if (icon != null) {
            iconView!!.visibility = VISIBLE
            iconView!!.setImageDrawable(icon.drawable)
        } else {
            iconView!!.visibility = GONE
        }
        if (tertiaryView != null) {
            tertiaryView!!.text = body
            nativeAdView!!.bodyView = tertiaryView
        }
        nativeAdView!!.setNativeAd(nativeAd)
    }

    /**
     * To prevent memory leaks, make sure to destroy your ad when you don't need it anymore. This
     * method does not destroy the template view.
     * https://developers.google.com/admob/android/native-unified#destroy_ad
     */
    fun destroyNativeAd() {
        nativeAd!!.destroy()
    }

//    val templateTypeName: String
//        get() {
//            if (templateType == R.layout.gnt_medium_template_view) {
//                return MEDIUM_TEMPLATE
//            } else if (templateType == R.layout.gnt_small_template_view) {
//                return SMALL_TEMPLATE
//            }
//            return ""
//        }

    private fun initView(context: Context, attributeSet: AttributeSet?) {
        val attributes =
            context.theme.obtainStyledAttributes(attributeSet, R.styleable.TemplateView, 0, 0)
        templateType = try {
            attributes.getResourceId(
                R.styleable.TemplateView_gnt_template_type, R.layout.new_native_medium_ad
            )
        } finally {
            attributes.recycle()
        }
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(templateType, this)
    }

    public override fun onFinishInflate() {
        super.onFinishInflate()
        nativeAdView = findViewById<View>(R.id.native_ad_view) as NativeAdView
        primaryView = findViewById<View>(R.id.primary) as TextView
        secondaryView = findViewById<View>(R.id.secondary) as TextView
        tertiaryView = findViewById<View>(R.id.body) as TextView?
        adPrice = findViewById<View>(R.id.adPrice) as TextView?
        ratingBar = findViewById<View>(R.id.rating_bar) as TextView
        ratingBarimg = findViewById<View>(R.id.adRate) as ImageView
        ratingBar!!.isEnabled = false
        callToActionView = findViewById<View>(R.id.cta) as Button
        iconView = findViewById<View>(R.id.icon) as ImageView
        mediaView = findViewById<View>(R.id.media_view) as MediaView?
        background = findViewById<View>(R.id.background) as ConstraintLayout
    }

    companion object {
        const val MEDIUM_TEMPLATE = "medium_template"
        const val SMALL_TEMPLATE = "small_template"
    }
}


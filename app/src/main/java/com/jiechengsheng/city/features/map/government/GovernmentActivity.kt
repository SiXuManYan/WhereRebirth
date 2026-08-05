package com.jiechengsheng.city.features.map.government

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.location.Address
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import biz.laenger.android.vpbs.BottomSheetUtils
import biz.laenger.android.vpbs.ViewPagerBottomSheetBehavior
import com.blankj.utilcode.util.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.response.MechanismResponse
import com.jiechengsheng.city.api.response.category.Category
import com.jiechengsheng.city.base.BaseEvent
import com.jiechengsheng.city.base.EventCode
import com.jiechengsheng.city.base.mvp.BaseMvpActivity
import com.jiechengsheng.city.features.map.CustomInfoWindowAdapter
import com.jiechengsheng.city.features.map.MechanismAdapter
import com.jiechengsheng.city.features.map.MechanismPagerAdapter
import com.jiechengsheng.city.features.mechanism.MechanismActivity
import com.jiechengsheng.city.features.search.SearchAllActivity
import com.jiechengsheng.city.features.store.filter.ThirdCategoryAdapter
import com.jiechengsheng.city.utils.CacheUtil
import com.jiechengsheng.city.utils.Constant
import com.jiechengsheng.city.utils.LocationUtil
import com.jiechengsheng.city.utils.PermissionUtils
import com.jiechengsheng.city.widget.list.DividerDecoration
import org.greenrobot.eventbus.EventBus
import com.jiechengsheng.city.databinding.ActivityGovernmentBinding
import android.view.ViewGroup

/**
 * Created by Wangsw  2021/8/24 17:04.
 * 闂佽　鍋撻柛顭戝枤閻╁酣鏌￠崼銏犲妺闁?
 * @see <a href="https://github.com/SiXuManYan/android-samples/blob/main/ApiDemos/kotlin/app/src/gms/java/com/example/kotlindemos/MarkerDemoActivity.kt">MarkerDemoActivity.kt</a>
 * @see <a href="https://github.com/SiXuManYan/android-samples/blob/main/ApiDemos/java/app/src/gms/java/com/example/mapdemo/MarkerDemoActivity.java">MarkerDemoActivity.java</a>
 */
class GovernmentActivity : BaseMvpActivity<GovernmentPresenter>(), GovernmentView {

    private lateinit var binding: ActivityGovernmentBinding


    /** 闂佸憡鍔曢幊搴敊閹版澘妞?tab婵炲瓨绮岄惉鍏碱殽閸ヮ剙绀嗛柛鈩冪懆椤?*/
    private lateinit var mPagerAdapter: MechanismPagerAdapter

    /** 婵炴垶鎸搁ˇ杈殽閸ヮ剙绀嗛柛鈩冪懆椤?*/
    private lateinit var mChildTagAdapter: ThirdCategoryAdapter

    /** marker 闂備緡鍋勯ˇ顕€鎳欓幋锕€瑙﹂幖鎼灣缁€澶愭倵閻㈠灚鍤€缂併劍鐓￠幆鍐礋椤愩垻浠氶柣鐐寸◤閸斿矂宕€电硶鍋?*/
    private lateinit var mMarkerContentAdapter: MechanismAdapter

    /** pager Behavior */
    private lateinit var pagerBehavior: ViewPagerBottomSheetBehavior<LinearLayout>

    /** maker  Behavior */
    private lateinit var makerBehavior: BottomSheetBehavior<RecyclerView>


    /** 婵炲濮存鎼佸垂鎼达絿灏电紒顐ゎ嚛b闁荤姴鎼悿鍥归崱娑樼睄閻犱礁婀辩粈澶愭煛瀹ュ牜娼愮€?categoryId  闁荤姴顑呴崯顖炲汲椤ょ彻ewPager 婵炶揪绲界粔鍫曟偪?*/
    private var childCategoryId = 0


    public val ID_GOVERNMENT = 1

    /** 閻熸粎澧楅幐鍛婃櫠閻樿鐭楅柛灞剧懅閻熴垽鎮归崶顒佹暠闁活亙鍗抽幆鍐礋椤愩垻鈧崵绱掗銈嗘緜d */
    private var currentRequestCategoryId = 1


    /** 闂佺懓鍚嬬划搴ㄥ磼閵娾晛绀冮柛娑卞弾閸?*/
    var searchInput: String? = null


    /** 婵犮垼娉涚€氼噣骞冩繝鍥х婵犲﹤鎳庨崑?*/
    private val searchLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val bundle = it.data?.extras
            searchInput = bundle?.getString(Constant.PARAM_NAME, "")
            search()
        }
    }

    private fun search() {
        binding.deleteIv.visibility = if (searchInput.isNullOrBlank()) {
            View.GONE
        } else {
            View.VISIBLE
        }
        binding.searchTv.text = searchInput
        EventBus.getDefault().post(BaseEvent<String?>(EventCode.EVENT_REFRESH_CHILD, searchInput))
        presenter.getMakerData(currentRequestCategoryId, searchInput)
    }


    override fun getLayoutId() = R.layout.activity_government

    override fun isStatusDark() = true

    @SuppressLint("NotifyDataSetChanged")
    override fun initView() {
        binding = ActivityGovernmentBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)
//        BarUtils.setStatusBarColor(this, ColorUtils.getColor(R.color.white))
        initExtra()
        initCategoryChild()
        initBehavior()
        initMarkerClickListContent()
    }

    private fun initExtra() {
        childCategoryId = intent.getIntExtra(Constant.PARAM_CHILD_CATEGORY_ID, 0)
    }

    fun getTotalCountView(): TextView {
        return binding.totalCountTv
    }

    /**
     * 婵炴垶鎸搁ˇ杈殽閸ヮ剙绀嗛柛鈩冪懆椤?
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun initCategoryChild() {

        mPagerAdapter = MechanismPagerAdapter(supportFragmentManager)

        mChildTagAdapter = ThirdCategoryAdapter().apply {
            setOnItemClickListener { _, _, position ->

                val child = mChildTagAdapter.data
                // 闁荤姳绀佹晶浠嬫偪閸℃稒鐒诲璺侯槼閸?
                child.forEachIndexed { index, category ->
                    category.nativeIsSelected = index == position
                }
                mChildTagAdapter.notifyDataSetChanged()

                // 闂佸憡甯￠弨閬嶅蓟婵犲嫮椹虫繛鎴旀噰閸嬫挻寰勬繝鍌滀粴闁?
                val target = mChildTagAdapter.data[position]
                EventBus.getDefault().post(BaseEvent<String>(EventCode.EVENT_REFRESH_MECHANISM, target.id.toString()))

                // 闂佸搫娲ら悺銊╁蓟?maker
                if (!::googleMap.isInitialized) return@setOnItemClickListener


                currentRequestCategoryId = child[position].id
                presenter.getMakerData(currentRequestCategoryId, searchInput)

            }
        }
        binding.childTagRv.apply {
            adapter = mChildTagAdapter
            layoutManager = LinearLayoutManager(this@GovernmentActivity, LinearLayoutManager.HORIZONTAL, false)
            addItemDecoration(
                DividerDecoration(Color.TRANSPARENT, SizeUtils.dp2px(16f), 0, 0)
            )
        }
    }


    /**
     * 婵犮垼娉涚€氼噣骞冩繝鍐╁劅闁哄洢鍨归崝銉╂煠鏉堛劍鐓ュ┑?
     */
    private fun initBehavior() {
        BottomSheetUtils.setupViewPager(binding.contentVp)
        pagerBehavior = ViewPagerBottomSheetBehavior.from(binding.bottomSheetLl)

        pagerBehavior.state = ViewPagerBottomSheetBehavior.STATE_EXPANDED


        makerBehavior = from(binding.bottomSheetRv)
        makerBehavior.state = STATE_HIDDEN
        makerBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {

            override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit

            override fun onStateChanged(bottomSheet: View, newState: Int) {

                when (newState) {
                    STATE_DRAGGING -> {
                        // 闂佸憡纰嶉崹宕囩箔閸岀偛绠ｉ柡宥庡厴閸嬫捇宕ㄩ鐔封偓璇测槈閹炬剚鍎愰悗姘儔瀹曟繈鍩￠悗姒痶om sheet
                    }
                    STATE_SETTLING -> {
                        // 闁荤喐鐟ュΛ妤€霉濡崵顩烽幖娣€曟慨搴ｇ磼閸屾粎甯涘褎绮撻獮鎰板炊椤忓嫭顔囬梺姹囧灱妞村摜鍒掗敃鍌氱闁靛鍎遍悡鍌炴煛閸艾浜剧紓鍌欑閻楀棙绂掗悩宕団枖閻庯綆鍓氶悾閬嶅级閳哄倻鎳佺紒鏂款嚟娴滄瓕绠涢弮鎴濇杸闂佸搫鍟悥鐓幬?
                    }
                    STATE_EXPANDED -> {
                        //婵犮垼娉涚€氼亞鑺遍鍌楀亾閻熸媽瀚伴柛娆忕箳娴狅箓寮撮悙鐢碉紱闂佹眹鍔岀€氼喗鎱ㄩ幖浣哥畱?
                        pagerBehavior.state = ViewPagerBottomSheetBehavior.STATE_COLLAPSED
                    }
                    STATE_COLLAPSED -> {
                        //婵帗绋掗…鍫ヮ敇婵犳碍鍎嶉柛鏇ㄥ墮椤垿鏌涘▎鎴澬ュ┑顔芥倐楠炩偓?
                    }
                    STATE_HIDDEN -> {
                        //婵炴垶鎸搁鍡欏垝閿曞倸绀夐柕濞垮劤閺嗘岸鏌涜箛瀣婵炲牞缍侀幐?
                        pagerBehavior.state = ViewPagerBottomSheetBehavior.STATE_EXPANDED
                    }

                    else -> {
                    }
                }


            }

        })

    }


    /**
     * Maker 闂備緡鍋勯ˇ顕€鎳欓幋锕€瑙﹂幖鎼灣缁€澶愭倵閻㈠灚鍤€缂併劍鐓￠幆鍐礋椤愩垻浠氶柣鐐寸◤閸斿秹寮抽悢鐓庣?
     */
    private fun initMarkerClickListContent() {

        mMarkerContentAdapter = MechanismAdapter().apply {
            showClose = true
            addChildClickViewIds(R.id.close_iv)
            setOnItemChildClickListener { _, _, _ ->
                makerBehavior.state = STATE_HIDDEN
            }
            setOnItemClickListener { _, _, position ->
                val data = this.data[position]
                startActivity(MechanismActivity::class.java, Bundle().apply {
                    putInt(Constant.PARAM_ID, data.id)
                })
            }
        }

        binding.bottomSheetRv.apply {
            adapter = mMarkerContentAdapter
            // 缂備礁鍊烽懗鍫曞极閵堝憘鐔碱敂閸曨剙鈧粯绻濇繝鍐ㄧ仴濠?
            layoutManager = object : LinearLayoutManager(context, HORIZONTAL, false) {
                override fun canScrollHorizontally() = false
            }
        }
        val pagerSnapHelper = PagerSnapHelper()
        pagerSnapHelper.attachToRecyclerView(binding.bottomSheetRv)

    }

    override fun initData() {
        presenter = GovernmentPresenter(this)
        initMap()
        presenter.apply {
            getGovernmentChildCategory()
        }

    }


    override fun bindListener() {

        binding.backIv.setOnClickListener { finish() }

        // tabLayout viewpager 闂佽壈椴搁弻銊︽叏?
        binding.contentVp.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit

            override fun onPageScrollStateChanged(state: Int) = Unit

            override fun onPageSelected(position: Int) {

                mChildTagAdapter.setNewInstance(null)
                binding.childTagRv.visibility = View.GONE

                val category = mPagerAdapter.category
                if (category.isEmpty()) {
                    return
                }

                // 闂佸搫娲ら悺銊╁蓟婵犲啰鈻斿璺虹焸閻涙捇鏌涢幒鎴烆棤閻?
                val child = category[position]
                if (child.has_children == 2 && child.child_categories.isNotEmpty()) {
                    mChildTagAdapter.setNewInstance(child.child_categories)
                    binding.childTagRv.visibility = View.VISIBLE
                } else {
                    binding.childTagRv.visibility = View.GONE
                }

                // 闂佸吋鍎抽崲鑼鏉堚晙娌柡鍥╁О娴犳盯鏌涢敂鍝勫婵犫偓閹绢喖鐐婇柛娆嶅劤閻熸劙鏌ｉ妸銉ヮ仾闁哄棛鍠栭獮?
                if (!::googleMap.isInitialized) return

                currentRequestCategoryId = category[position].id
                presenter.getMakerData(currentRequestCategoryId)
                makerBehavior.state = STATE_HIDDEN
            }
        })

        binding.searchTv.setOnClickListener {
            searchLauncher.launch(Intent(this, SearchAllActivity::class.java).putExtra(Constant.PARAM_TYPE, 3))
            makerBehavior.state = STATE_HIDDEN
        }

        binding.deleteIv.setOnClickListener {
            searchInput = null
            search()
        }

        binding.topDragLl.setOnClickListener {
            when (pagerBehavior.state) {

                ViewPagerBottomSheetBehavior.STATE_COLLAPSED -> {
                    pagerBehavior.state = ViewPagerBottomSheetBehavior.STATE_EXPANDED
                }
                ViewPagerBottomSheetBehavior.STATE_EXPANDED -> {
                    pagerBehavior.state = ViewPagerBottomSheetBehavior.STATE_COLLAPSED
                }
                else -> {}
            }


        }
    }


    override fun bindSecondCategory(response: ArrayList<Category>) {

        // pager
        mPagerAdapter.category.addAll(response)
        mPagerAdapter.notifyDataSetChanged()
        binding.contentVp.offscreenPageLimit = response.size
        binding.contentVp.adapter = mPagerAdapter
        binding.tabsType.setViewPager(binding.contentVp)

        // 濠电姴锕ラ懝鐐叏閳哄懎绀嗛柟娈垮枟缁愭鏌″鍛煑缂傚秴鎳愮槐?
        if (childCategoryId > 0) {
            var targetIndex = 0
            response.forEachIndexed { index, category ->
                if (category.id == childCategoryId) {
                    targetIndex = index
                    return@forEachIndexed
                }
            }
            binding.contentVp.currentItem = targetIndex
        }

    }


    // ################ 闂侀潻闄勫妯好瑰鈧幆鍕矙閸喖缍?###################
    private lateinit var googleMap: GoogleMap

    // 闂佺懓鐡ㄩ崹鐟扳枔閹寸偞濯寸€广儱娲ㄩ弸?
    private lateinit var myLocation: CameraPosition

    // 闂侀潻闄勫妯好瑰Ο鑽も枖濠电姴瀚悾閬嶆煙绾版ê浜鹃梺鍝勭墕閵嗗¨ker
    private var makers: ArrayList<Marker?> = ArrayList()

    private fun initMap() {

        // 闂佸吋鍎抽崲鑼躲亹?SupportMapFragment 濡ょ姷鍋犲▔娑橈耿椤忓牆鎹堕柡澶嬪缁傚牓鏌涢幋婵囶棞妞わ富鍓氱缓浠嬪箣閹烘垟鏋忛梺娲绘娇閸斿秴顪冮崒婊勫珰闂佸灝顑囧﹢鎾⒑椤愶絾鐨戦柣?
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        googleMap.apply {

            // 缂備礁顦抽褎鎱ㄩ埡鍛闁哄鐏濆浠嬫煛閸噥娈樺?
            moveCamera(CameraUpdateFactory.newLatLng(LatLng(Constant.LAT, Constant.LNG)))


            // 闁荤姴顑呴崯顖炲汲閿濆绀冮柛娑卞灣閺嬩繃I padding 闂傚倸鍟鍫曨敆濞岀go闁荤偞鍑归崑濠冨閸曨垰绠?
            setPadding(0, 0, 0, SizeUtils.dp2px(120f))

            // 闂佺厧顨庢禍婊堟偩閻愵剛鈻曞璺侯槷缁诲棝鏌熼褍鐏￠柣妤€宕?
            setInfoWindowAdapter(CustomInfoWindowAdapter(this@GovernmentActivity))

            // 闂佺粯鍔楅幊鎾诲吹椤曗偓瀵粙宕堕宥呮暪
            setOnMarkerClickListener(this@GovernmentActivity)

            // 闂佺粯鍔楅幊鎾诲吹椤曗偓瀵粙宕堕宥呮暪婵烇絽娲犻崜婵囧閸涱垳鐜绘俊銈傚亾鐟?
            setOnInfoWindowClickListener(this@GovernmentActivity)

            // 缂備礁顦抽褎鎱ㄩ埡鍛闁绘鐗嗛悘澶愭煟閵娿儱顏х紓宥呮噽缁?
            setOnMyLocationButtonClickListener(this@GovernmentActivity)

            // 闂佺粯鍔楅幊鎾诲吹椤曗偓楠炲骞嬮悩宕囨殸婵炶揪绲界粔鍫曟偪?
            setOnMyLocationClickListener(this@GovernmentActivity)
            // 闁哄鐗嗛幊搴㈡叏椤忓牆绀夐柣鏃囶嚙閸樺瓨淇婇妞诲亾瀹曞洨顢呴梺鎸庣☉閻線銆呴锔藉剮闁哄稁鍓﹀鎺楁煕閵夈儱缍栫紒妤€鍊块幆鍐礋椤栨粌绗￠柣鐘遍檷閸婃洟宕€电硶鍋撶涵鍜佹綈鐎殿喗瀵у?
            setContentDescription("Map with lots of markers.");
        }

        googleMap.uiSettings.apply {
            isMapToolbarEnabled = true
            // 闂傚倸鎳忛崝妯何涘畝鍕闁瑰濮甸悾鍗灻归敐鍛ら柣銈呮瀹曨亞浠﹂懞銉ь洯闁?
//            isMyLocationButtonEnabled = false

        }


        // 閻熸粎澧楅幐鍛婃櫠閻樿櫕濯寸€广儱娲ㄩ弸?
        enableMyLocation()

        // 闂佸吋鍎抽崲鑼鏉堚晙娌柡鍥╁О娴犳盯鏌涢敂鍝勫婵犫偓閹绢喖鐐婇柛娆嶅劤閻熸劙鏌ｉ妸銉ヮ仾闁哄棛鍠栭獮?
        presenter.getMakerData(ID_GOVERNMENT, searchInput)

    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (!::googleMap.isInitialized) return

        PermissionUtils.permissionAny(
            this, {
                if (it) {
                    googleMap.isMyLocationEnabled = true
                    getMyLocationInfo()
                }
            }, Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

    }

    /**
     * 闂佸吋鍎抽崲鑼躲亹閸ヮ剙绠ｉ柟瀵稿У閻ｅ崬霉閿濆懐肖闁汇倕妫欑粚閬嶅焺閸愌呯
     */
    private fun getMyLocationInfo() {
        LocationUtil.getInstance().addressCallback = object : LocationUtil.AddressCallback {
            override fun onGetAddress(address: Address) {
                val countryName = address.countryName //闂佹悶鍎村Λ鍕敊?
                val adminArea = address.adminArea //闂?
                val locality = address.locality //闁?
                val subLocality = address.subLocality //闂?
                val featureName = address.featureName //闁荤偞绋掗〃濠冪?
            }

            override fun onGetLocation(lat: Double, lng: Double) {
                CacheUtil.getShareDefault().put(Constant.SP_MY_LATITUDE, lat.toFloat())
                CacheUtil.getShareDefault().put(Constant.SP_MY_LONGITUDE, lng.toFloat())


                myLocation = CameraPosition.Builder().target(LatLng(lat, lng))
                    .zoom(20f)
                    .bearing(0f)
                    .tilt(0f)
                    .build()
            }
        }
    }


    /**
     * 缂備礁顦抽褎鎱ㄩ埡鍛闁绘鐗嗛悘澶愭煟閵娿儱顏х紓宥呮噽缁?
     */
    override fun onMyLocationButtonClick(): Boolean {
        if (!::myLocation.isInitialized) return false
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(myLocation))
        return false
    }

    /**
     * 闂佺粯鍔楅幊鎾诲吹椤曗偓楠炲骞嬮悩宕囨殸婵炶揪绲界粔鍫曟偪?maker
     */
    override fun onMyLocationClick(location: Location) {
//        ToastUtils.showShort("Current location : \nlatitude:${location.latitude}" + "\nlongitude:${location.longitude}  ")
    }


    override fun bindMakerList(response: ArrayList<MechanismResponse>) {
        if (!::googleMap.isInitialized) return

        if (response.isEmpty()) {
            ToastUtils.showShort(R.string.search_result_empty_hint)
            googleMap.clear()
            makers.clear()
            return
        }


        // 闂佺儵鏅涢幉鈥斥攦閳ь剛绱掓径搴殭濠殿喒鏅犲畷姘舵儌缁烘笒er闂佽偐鍘ч崯顐⒚?
        val bounds = LatLngBounds.Builder()

        response.forEach {
            bounds.include(LatLng(it.lat, it.lng))
        }
        if (DeviceUtils.isTablet()) {
            if (response.isNotEmpty()) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(response[0].lat, response[0].lng),15f))
            }
        }else {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 300))
        }

        // 闂侀潻璐熼崝灞斤耿閹绢喖鐐婇柛娆嶅劤閻熸劖绻涢敐鍫殭濠殿喚鍋炲鍕樄闁革絿鐦瞐rker
        addMarkersToMap(response)

        // 闁诲繒鍋炲ú婊堝Φ濮濈敘rker 闂佸憡甯楅〃澶愬Υ閸愵喖鏋侀柣妤€鐗嗙粊?
        mMarkerContentAdapter.setNewInstance(response)

    }

    /**
     * 闂佸憡纰嶉崹闈涳耿閹绢喖鐐婇柛娆嶅劥閸橆剚绻涢敐鍫殭濠?Maker
     */
    private fun addMarkersToMap(response: ArrayList<MechanismResponse>) {
        if (!::googleMap.isInitialized || response.isEmpty()) return
        googleMap.clear()
        makers.clear()

        response.forEach {

            val view = LayoutInflater.from(this).inflate(R.layout.custom_info_contents_2, null)
            val image = view.findViewById<ImageView>(R.id.image_iv)
            image.setImageResource(R.mipmap.ic_marker_common)

            val title_tv = view.findViewById<TextView>(R.id.title_tv)
            title_tv.text = it.title

            val maker = googleMap.addMarker(
                MarkerOptions()
                    .position(LatLng(it.lat, it.lng))
                    .title(it.title)
                    .snippet("")
                    .icon(BitmapDescriptorFactory.fromBitmap(ConvertUtils.view2Bitmap(view)))
            )
            maker?.tag = it
            makers.add(maker)
        }

    }


    /**
     * 闂佺粯鍔楅幊鎾诲吹椤曗偓瀵粙宕堕宥呮暪
     */
    override fun onMarkerClick(marker: Marker): Boolean {

        // 闂佸湱顣介崑鎾绘煛?maker 闁荤姳绀佹晶浠嬫偪閸℃稑绠ｉ柟閭﹀幗瀵捇姊洪銏╂Х闁?
        makers.forEach {
            val makerTag = it?.tag as MechanismResponse
            val view = LayoutInflater.from(this).inflate(R.layout.custom_info_contents_2, null)
            val title_tv = view.findViewById<TextView>(R.id.title_tv)
            title_tv.text = makerTag.title
            val image_iv = view.findViewById<ImageView>(R.id.image_iv)
            image_iv.setImageResource(R.mipmap.ic_marker_common)

            it.setIcon(BitmapDescriptorFactory.fromBitmap(ConvertUtils.view2Bitmap(view)))

        }
        marker.hideInfoWindow()

        // 闁荤姳绀佹晶浠嬫偪閸℃哎浜归柟鎯у暱椤ゅ懘姊洪銏╂Х闁?
        val currentMakerTag = marker.tag as MechanismResponse
        val view = LayoutInflater.from(this).inflate(R.layout.custom_info_contents_2, null)
        val title_tv = view.findViewById<TextView>(R.id.title_tv)
        title_tv.text = currentMakerTag.title
        val image_iv = view.findViewById<ImageView>(R.id.image_iv)
        image_iv.setImageResource(R.mipmap.ic_marker_select)
        marker.setIcon(BitmapDescriptorFactory.fromBitmap(ConvertUtils.view2Bitmap(view)))

        // 闂佸憡甯掑ú锕€鐣烽崣澶嬪劅闁哄洢鍨归崝銉╂煕閹烘搩娈欓柕鍡楀暣瀵偊鎮ч崼婵堛偊
        val index = makers.indexOf(marker)
        if (index != -1) {
            binding.bottomSheetRv.scrollToPosition(index)
            makerBehavior.state = STATE_EXPANDED
        }

        // 闂侀潻闄勫妯好瑰鈧悰顕€鎼归銈嗗缂備礁顦抽褎鎱ㄩ埡鍛闁规鍠楃粣妤呮煛瀹ュ懏鐭楃紓宥呮噽缁?
        Handler(mainLooper).postDelayed({
            val targetCamera = CameraPosition.Builder().target(marker.position)
                .zoom(15.5f)
                .bearing(0f)
                .tilt(0f)
                .build()
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(targetCamera))

        }, 10)

        return false
    }

    /**
     * 闂佺粯鍔楅幊鎾诲吹椤曗偓瀵粙宕堕宥呮暪婵炴垶鎸搁敃锕€鈻撻幋鐐碘攳闁斥晛鍟╃槐鏍磼閹邦収娈旂憸?
     */
    override fun onInfoWindowClick(marker: Marker) = Unit


}

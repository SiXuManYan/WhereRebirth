package com.jiechengsheng.city.features.travel.map


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Address
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.response.CityPickerResponse
import com.jiechengsheng.city.api.response.category.Category
import com.jiechengsheng.city.api.response.travel.TravelChild
import com.jiechengsheng.city.base.mvp.BaseMvpActivity
import com.jiechengsheng.city.features.map.HotelCustomInfoWindowAdapter
import com.jiechengsheng.city.features.search.SearchAllActivity
import com.jiechengsheng.city.features.travel.detail.TravelDetailActivity
import com.jiechengsheng.city.features.travel.map.child.TravelChildAdapter
import com.jiechengsheng.city.features.travel.map.filter.TravelCategoryFilterAdapter
import com.jiechengsheng.city.features.travel.map.filter.TravelCityFilterAdapter
import com.jiechengsheng.city.utils.CacheUtil
import com.jiechengsheng.city.utils.Constant
import com.jiechengsheng.city.utils.LocationUtil
import com.jiechengsheng.city.utils.PermissionUtils
import com.jiechengsheng.city.view.empty.EmptyView
import com.jiechengsheng.city.widget.list.DividerDecoration
import com.jiechengsheng.city.databinding.ActivityTravelMapBinding
import android.view.ViewGroup
/**
 * Created by Wangsw  2021/10/18 9:37.
 *  闂備礁鎼崯顐﹀箠韫囨稒鍋夐柛顐ｆ礀閹瑰爼鏌℃径瀣嚋缂?
闂備礁鎲￠悧鏇㈠箹椤愶附鍋傛慨妤婃弓 濠电偞娼欓崥瀣晪闂佸憡蓱缁嬫捇鎯€椤忓浂妲煎┑鐐存崄閸╂牠銆冮崶褜娼ㄩ柛顐犲灮椤?
濠碘槅鍋撶徊楣冩偋濡ゅ拋鏁冮柛蹇撳悑娴溿倖绻涢幋鐐垫噭闂傚嫬绉归弻锝夛綖椤掆偓婵¤法绱掗鍡欑？闁汇儺浜、妤呭焵椤掑倵鍋撻悷鎵紞闁归濞€閺屻劎鈧綆鍓氶崟楣冩⒑閹稿海鈽夐柤娲诲灦閸┾偓妞ゆ帒瀚敍鐔兼偣閹邦喖鏋涢柟顔荤矙婵℃悂鏁冮埀顒勬偂閺囥垺鐓欓梻鍫熶緱閸庡繐鈹戦瑙勬珚闁哄苯鎳忓鍕偓锝庝憾娴犳挳姊哄Ч鍥у閻庢凹鍠氶崚鎺楁晬閸曨厽锛忓銈嗗坊閸嬫捇鎮楃憴鍕枙闁轰礁绉瑰畷濂告偄缁嬭法澶勯梻鍌欑贰閸嬪嫰藝閹殿喒鍋撳銉ュ鐎殿噮鍠氶幑鍕传閸曨厽鍋у┑顔藉笚缁嬫帡鈥﹂崼銉晣濠电姵鑹剧壕鍦棯椤撱埄妫戠紒鈧?
闂傚倷绶￠崑鍕囬幍顔瑰亾濮樸儱濮傜€规洘顨婃俊鎼佸煛娴ｇ袚闂備胶纭堕弲娑欘殽閸濄儳鍗氶柛顐ｆ礀缁秹鎮规担鍛婅础缂佲偓婢舵劖鐓ユ繛鎴烆焽婢с垽鏌℃担闈涒偓妤冩閹烘洜鐤€闁哄洨鍋為崕鎾绘煙閼圭増褰х紒鐘冲浮椤㈡岸顢楅埀顒佹櫏闂佺鐭夋俊鍥╃矆?
闂備礁鎲￠悧鏇㈠箹椤愶附鍋傛俊鎯х暢濠? 闂備焦瀵х粙鎴︽儔閼测晛鍨濋柨娑樺濡垱銇勯幒鍡椾壕闁诲海顢婂▍鏇㈠箯閸涱垱宕夐柕濠忛檮濞堛垽姊鸿ぐ鎺撴锭妞ゆ垵鎳忛幈銊╁煛閸涱喚鍔垫繝銏ｆ硾椤戝懘顢旈妷褏纾藉ù锝呮憸缁犳娊鏌涘鐓庝簼闁?
闂傚倷绶￠崑鍕囬幍顔瑰亾濮樸儱濮傜€规洘顨婂鐢告偨閻㈠憡鎯冮梻浣虹《閺呮稒顨ラ崫銉у崥闁割偅娲栫猾宥夋偣娴ｅ憡璐＄紒鈧径鎰厽妞ゎ偒鍓欐俊濂告倶韫囷絽鐏︾€规洘妞介幃鐐测柦濠靛鍋ｉ柛銉墯閺侀亶鏌?
 */
class TravelMapActivity : BaseMvpActivity<TravelMapPresenter>(), TravelMapView {

    private lateinit var binding: ActivityTravelMapBinding

    /** 闂備礁鎼崯顐﹀箠韫囨稒鍋夐悹杞扮秿閻旂厧鐏抽柧蹇ｅ亜娴犳挳姊洪崨濠傜瑨婵☆偅绋撻幃顕€鎮抽々?*/
    private var travelCategoryId = 0


    /** 闂備胶鎳撻崥瀣垝鎼淬劌纾奸柕濞炬櫅缁€鍐煕濞戝崬寮鹃柛?*/
    var searchInput: String? = null


    /** marker 闂傚倷绶￠崑鍕囬鈧幊娆撳箣閿曗偓鐟欙箓骞栫紒鑸€曟晶楣冩⒑閸涘﹤鐏辨繛鍜冪到閳?item */
    private lateinit var mMarkerContentAdapter: TravelMarkerSelectedAdapter

    /** maker  Behavior */
    private lateinit var makerBehavior: BottomSheetBehavior<RecyclerView>

    /** 闂備礁鎲￠悧鏇㈠箹椤愶箑鍨傞幖娣妼閹瑰爼鏌℃径瀣嚋缂佸倸鐗撻弻娑橆煥閸愵厼顏梺鍓茬厛娴滅偤骞冩导鏉懳╅張顏呮媴閸撴彃娈?*/
    private var contentIsMap = false

    /** 闂備胶纭堕弲娑欘殽閸濄儳鍗氶柛鎾茶兌妞硅櫕绻涢幋鏃€鍣伴柛?*/
    private lateinit var cityFilterAdapter: TravelCityFilterAdapter

    /** 闂備礁鎲＄敮鎺懳涘☉姘仏妞ゆ劑鍊楁す铏箾閹存梹鍣伴柛?*/
    private lateinit var categoryFilterAdapter: TravelCategoryFilterAdapter

    /** 缂傚倷鑳堕搹搴ㄥ礈濞嗘垟鍋撻悷鎵紞缂侇噣娼ч埥澶愬煑閳轰椒澹?*/
    var requestLatitude = 0.0

    /** 缂傚倸鍊风粈浣烘崲閹邦喒鍋撻悷鎵紞缂侇噣娼ч埥澶愬煑閳轰椒澹?*/
    var requestLongitude = 0.0

    var requestAreaId = "0"

    /** 闂備礁鎲￠崝鏇㈠箠鎼搭煈鏁婇柟鐗堟緲缁€鍡樹繆閵堝懎顏ラ柍褜鍓欓崯鎵垝濮樿精濮抽柟顓熷悅w */
    private lateinit var emptyView: EmptyView

    /** 闂備礁鎲￠崝鏇㈠箠鎼搭煈鏁婇柟鐗堟緲缁€鍡樹繆閵堝懎顏ラ柍?*/
    private lateinit var mAdapter: TravelChildAdapter

    /** 闂備礁鎲￠崝鏇㈠箠鎼搭煈鏁婇柟鐗堟緲缁€鍡樹繆閵堝懎顏ラ柍?page */
    private var page = Constant.DEFAULT_FIRST_PAGE

    /** 闁荤喐绮庢晶妤呭箰閸涘﹥娅犻柣妯款嚙閻鏌涚仦鍓ф噮闁荤喆鍨介幃褰掑炊椤掍焦鏆犻梺娲讳簷閸楁娊骞嗛崘顔肩妞ゆ劑鍨婚埀顒冨吹缁辨帡顢曢妶鍡樼窚d */
    var currentRequestCategoryId = 0


    // ################ 闂備線娼婚梽鍕熆濡ソ鐟邦潨閳ь剟骞嗛崟顓犵煓闁割煈鍠栫紞?###################

    private lateinit var googleMap: GoogleMap

    // 闂備胶鎳撻悺銊╁垂閻熸壋鏋旈柟瀵稿仦婵鈧箍鍎卞ú銊╁几?
    private lateinit var myLocation: CameraPosition

    // 闂備線娼婚梽鍕熆濡ソ鐟拔熼懡銈傛灃婵犵數濮寸€氼參鎮鹃柆宥嗙厵缁剧増锚娴滈箖姊洪崫鍕闁靛棗篓ker
    private var makers: ArrayList<Marker?> = ArrayList()

    private var needMoveCamera = false

    override fun isStatusDark() = true

    override fun getLayoutId() = R.layout.activity_travel_map

    companion object {

        fun navigation(context: Context, categoryId: Int) {

            val bundle = Bundle().apply {
                putInt(Constant.PARAM_CATEGORY_ID, categoryId)
            }
            val intent = Intent(context, TravelMapActivity::class.java)
                .putExtras(bundle)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }


    /** 濠电姰鍨煎▔娑氣偓姘煎櫍楠炲啯绻濋崶褏顔愬┑鐘诧工閹冲酣宕?*/
    private val searchLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val bundle = it.data?.extras
            searchInput = bundle?.getString(Constant.PARAM_NAME, "")
            search()
        }
    }


    override fun initView() {
        binding = ActivityTravelMapBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)
        BarUtils.setStatusBarColor(this, ColorUtils.getColor(R.color.white))
        initExtra()
        initMap()
        initBehavior()
        initMarkerClickListContent()
        initFilter()
        initContentList()
    }

    /** 缂傚倷鐒︾粙鎺楁儎椤栫偛鐒?*/
    @SuppressLint("NotifyDataSetChanged")
    private fun initFilter() {

        // 缂傚倷鐒︾粙鎺楁儎椤栫偛鐒垫い鎺嗗亾妞わ富鍨跺畷褰掝敂閸℃ê浠?
        categoryFilterAdapter = TravelCategoryFilterAdapter()
        binding.travelFilter.categoryFilterRv.apply {
            adapter = categoryFilterAdapter
            layoutManager = LinearLayoutManager(this@TravelMapActivity, LinearLayoutManager.VERTICAL, false)
        }

        cityFilterAdapter = TravelCityFilterAdapter()
        binding.travelFilter.cityFilterRv.apply {
            adapter = cityFilterAdapter
            layoutManager = LinearLayoutManager(this@TravelMapActivity, LinearLayoutManager.VERTICAL, false)
        }

        cityFilterAdapter.setOnItemClickListener { _, _, position ->
            val data = cityFilterAdapter.data
            val item = data[position]
            data.forEach {
                it.nativeIsSelected = it.id == item.id
            }
            cityFilterAdapter.notifyDataSetChanged()

            binding.selectedCityTv.text = item.name
            requestLatitude = item.lat
            requestLongitude = item.lng
            requestAreaId = item.id
            page = Constant.DEFAULT_FIRST_PAGE
            binding.travelFilter.travelFilterLl.visibility = View.GONE
            requestContentList()
            requestMakerList()
            needMoveCamera = true
        }

        categoryFilterAdapter.setOnItemClickListener { _, _, position ->
            val data = categoryFilterAdapter.data
            val item = data[position]
            data.forEach {
                it.nativeIsSelected = it.id == item.id
            }
            categoryFilterAdapter.notifyDataSetChanged()

            binding.selectCategoryTv.text = item.name
            currentRequestCategoryId = item.id
            page = Constant.DEFAULT_FIRST_PAGE
            binding.travelFilter.travelFilterLl.visibility = View.GONE
            requestContentList()
            requestMakerList()
        }

        // UI闂備礁鎲￠崝鏇㈠箠鎼搭煈鏁婇柟鐗堟緲缁€鍡涙煕閵夛絽濡奸幖?
        binding.selectCategoryLl.setOnClickListener {
            when (binding.travelFilter.travelFilterLl.visibility) {
                View.VISIBLE -> {
                    // 濠电姷顣介埀顒€鍟块埀顒€缍婇幃妯诲緞閹邦剙寮峰銈嗙墬濮樸劎绮旀總鍛婄厱闁规儳纾倴闁荤偞鍑归崑濠傜暦閸︻厸鍋撻敐搴″闁伙綁浜堕幃姗€鎮欑€涙顦ョ紓浣介哺閸ㄥ潡寮鍛殕闁逞屽墮鐓ら柛褎顨呯粻鎶芥煏婢跺牆鍔氱憸鎵█濮?
                    if (binding.travelFilter.filterSw.displayedChild == 0) {
                        closeCategoryFilter()
                        binding.travelFilter.travelFilterLl.visibility = View.GONE
                    } else {
                        // 闂備礁鎲＄敮鎺懨洪敃鈧悾鐑藉蓟閵夈儳顦梺鍝勵槼濞夋洜绮旀總鍛婄厱闁规儳纾倴闁荤偞鍑归崑濠傜暦?
                        showCategoryFilter()
                    }
                }
                View.GONE -> showCategoryFilter()
                else -> {}
            }
        }

        binding.selectedCityLl.setOnClickListener {
            when (binding.travelFilter.travelFilterLl.visibility) {
                View.VISIBLE -> {
                    // 濠电姷顣介埀顒€鍟块埀顒€缍婇幃妯诲緞閹邦剙寮峰銈嗙墬濮樸劎绮旀總鍛婄厱闁规儳纾倴闁荤偞鍑归崑濠傜暦閸︻厸鍋撻敐搴″闁伙綁浜堕幃姗€鎮欑€涙顦ョ紓浣介哺閸ㄥ潡寮鍛殕闁逞屽墮鐓ら柛褎顨呯粻鎶芥煏婢跺牆鍔氱憸鎵█濮?
                    if (binding.travelFilter.filterSw.displayedChild == 1) {
                        closeCategoryFilter()
                        binding.travelFilter.travelFilterLl.visibility = View.GONE
                    } else {
                        // 闂備礁鎲＄敮鎺懨洪敃鈧悾鐑藉蓟閵夈儳顦梺鍝勵槼濞夋洜绮旀總鍛婄厱闁规儳纾倴闁荤偞鍑归崑濠傜暦?
                        showCityFilter()
                    }
                }
                View.GONE -> showCityFilter()
                else -> {}
            }

        }

        binding.travelFilter.dismissView.setOnClickListener {
            binding.selectCategoryIv.rotation = 0f
            binding.selectedCityIv.rotation = 0f
            binding.travelFilter.travelFilterLl.visibility = View.GONE
        }

    }

    private fun closeCategoryFilter() {
        binding.selectCategoryTv.isChecked = false
        binding.selectCategoryIv.rotation = 0f
    }

    private fun showCategoryFilter() {
        binding.travelFilter.travelFilterLl.visibility = View.VISIBLE
        binding.selectCategoryTv.isChecked = true
        binding.selectCategoryIv.rotation = 180f
        binding.travelFilter.filterSw.displayedChild = 0
        closeCityFilter()
    }

    private fun closeCityFilter() {
        binding.selectedCityTv.isChecked = false
        binding.selectedCityIv.rotation = 0f
    }

    private fun showCityFilter() {
        binding.travelFilter.travelFilterLl.visibility = View.VISIBLE
        binding.selectedCityTv.isChecked = true
        binding.selectedCityIv.rotation = 180f
        binding.travelFilter.filterSw.displayedChild = 1
        closeCategoryFilter()
    }


    private fun initContentList() {

        emptyView = EmptyView(this).apply {
            setEmptyImage(R.mipmap.ic_empty_search)
            setEmptyMessage(R.string.empty_search_message)
            setEmptyHint(R.string.empty_search_hint)
            addEmptyList(this)
        }

        mAdapter = TravelChildAdapter().apply {
            setEmptyView(emptyView)
            loadMoreModule.isAutoLoadMore = true
            loadMoreModule.isEnableLoadMoreIfNotFullPage = true
            setOnItemClickListener { _, _, position ->
                val data = mAdapter.data[position]
                TravelDetailActivity.navigation(this@TravelMapActivity, data.id)
            }
            loadMoreModule.setOnLoadMoreListener {
                page++
                requestContentList()
            }
        }

        binding.bottomSheetContentRv.apply {
            adapter = mAdapter
            layoutManager = LinearLayoutManager(this@TravelMapActivity, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(
                DividerDecoration(Color.TRANSPARENT, SizeUtils.dp2px(15f), 0, 0).apply {
                    setDrawHeaderFooter(true)
                })

        }


    }


    private fun initExtra() {
        val bundle = intent.extras ?: return
        bundle.apply {
            travelCategoryId = getInt(Constant.PARAM_CATEGORY_ID)
        }

        // 濠碘槅鍋撶徊楣冩偋濡ゅ拋鏁冮柛蹇撳悑娴溿倖绻涢幋鐐垫噭闂傚嫬绉归弻銊モ槈濡警娈紓浣诡殔椤︽壆鈧潧銈搁幃鈺呭箵閹烘顔冮梻浣虹《閺呮繈宕滅紞?闂備焦瀵х粙鎴︽嚐椤栨縿浜圭憸搴ｂ偓闈涖偢閹晠鎮介悽鍨挄缂傚倷鑳堕搹搴ㄥ礈濞嗘垟鍋撶憴鍕枙闁轰礁绉瑰畷鐓庘攽閸℃瑦鍋у┑鐐村灦閹告悂鏁冮妷鈺婃晝濠靛倸鎲￠悞璇差熆鐠轰警鍎忔い蹇嬪劦閺?
        val safeSelectLatLng = CacheUtil.getSafeSelectLatLng()
        requestAreaId = "0"
        requestLatitude = safeSelectLatLng.latitude
        requestLongitude = safeSelectLatLng.longitude
    }


    private fun initMap() {
        // 闂備礁鍚嬮崕鎶藉床閼艰翰浜?SupportMapFragment 婵°倗濮烽崑鐘测枖濞戞﹫鑰挎い蹇撶墕閹瑰爼鏌℃径瀣嚋缂佸倸鐗撻弻娑㈠箣濠靛浂妫炲銈忓瘜閸撴氨缂撴禒瀣闁圭儤鍨熼弸蹇涙⒑濞茬粯濞囬柛鏂跨Т椤啴宕掑鍕彴闂備礁鐏濋鍥э耿閹绢喗鈷戞い鎰剁稻閻ㄦ垿鏌?
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    private fun initBehavior() {


        makerBehavior = BottomSheetBehavior.from(binding.bottomSheetRv)
        makerBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        makerBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit
            override fun onStateChanged(bottomSheet: View, newState: Int) = when (newState) {
                BottomSheetBehavior.STATE_EXPANDED -> {
                    binding.bottomSheetContentRv.visibility = View.GONE
                    binding.swipeLayout.isEnabled = false
                }
                BottomSheetBehavior.STATE_HIDDEN -> {
                    binding.bottomSheetContentRv.visibility = View.VISIBLE

                    binding.swipeLayout.isEnabled = !binding.bottomSheetContentRv.canScrollVertically(-1)
                }
                else -> {
                }
            }
        })

        binding.bottomSheetContentRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                binding.swipeLayout.isEnabled = (!binding.bottomSheetContentRv.canScrollVertically(-1) && !contentIsMap)

            }
        })

    }

    private fun initMarkerClickListContent() {
        // 闂備胶绮崝妤呭箠閹捐鍚?marker 闂備礁鎲￠懝鍓р偓娑掓櫅铻ｅ璺虹灱閻棝鏌ц箛姘兼綈闁伙綁浜堕弻娑㈠箳閹垮啯鐣介梺?

        mMarkerContentAdapter = TravelMarkerSelectedAdapter().apply {
            setOnItemClickListener { _, _, position ->
                val data = this.data[position]
                TravelDetailActivity.navigation(this@TravelMapActivity, data.id)
            }
        }

        val pagerSnapHelper = PagerSnapHelper()
        pagerSnapHelper.attachToRecyclerView(binding.bottomSheetRv)

        binding.bottomSheetRv.apply {
            adapter = mMarkerContentAdapter
            // 缂傚倷绀侀崐鐑芥嚄閸洖鏋侀柕鍫濇啒閻旂⒈鏁傞柛鏇ㄥ墮閳ь剛绮换婵囩節閸愩劎浠存繝?
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                    newState == RecyclerView.SCROLL_STATE_IDLE
                }
            })
        }

    }


    override fun initData() {
        presenter = TravelMapPresenter(this)
        presenter.getGovernmentChildCategory(travelCategoryId)
        presenter.getCityList()
        requestContentList()
    }

    override fun bindListener() {
        binding.swipeLayout.setOnRefreshListener {
            when {
                cityFilterAdapter.data.isEmpty() -> {
                    presenter.getCityList()
                }
                categoryFilterAdapter.data.isEmpty() -> {
                    presenter.getCityList()
                }
                mMarkerContentAdapter.data.isEmpty() -> {
                    requestMakerList()
                }
            }
            page = Constant.DEFAULT_FIRST_PAGE
            requestContentList()
        }

        binding.searchTv.setOnClickListener {
            searchLauncher.launch(Intent(this, SearchAllActivity::class.java).putExtra(Constant.PARAM_TYPE, 4))
            //  makerBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
        binding.typeIv.setOnClickListener {

            if (contentIsMap) {
                binding.typeIv.setImageResource(R.mipmap.ic_type_map)
                makerBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            } else {
                binding.typeIv.setImageResource(R.mipmap.ic_type_list)
                makerBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
            contentIsMap = !contentIsMap

        }


        binding.backIv.setOnClickListener {
            finish()
        }

        binding.deleteIv.setOnClickListener {
            searchInput = null
            search()
        }

    }

    override fun bindSecondCategory(response: ArrayList<Category>) {
        categoryFilterAdapter.setNewInstance(response)
    }


    private fun search() {
        binding.deleteIv.visibility = if (searchInput.isNullOrBlank()) {
            View.GONE
        } else {
            View.VISIBLE
        }
        binding.searchTv.text = searchInput
        requestMakerList()

        page = Constant.DEFAULT_FIRST_PAGE
        requestContentList()
    }

    /** 闂佽崵濮村ú顓㈠绩闁秵鍎戝ù鍏兼綑缁€鍐煕濞戝崬寮鹃柛鐔锋喘閺屾盯骞掗幙鍐╃暯闂? */
    private fun requestContentList() =
        presenter.getContentList(page, currentRequestCategoryId, searchInput, requestLatitude, requestLongitude, requestAreaId)


    // #############  闂備線娼婚梽鍕熆濡ソ鐟邦潨閳ь剟骞嗛崟顓犵煓闁割煈鍠栫紞?####################

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        googleMap.apply {

            // 缂傚倷绀侀ˇ鎶筋敋瑜庨幈銊╁煛閸涱厾顦梺鍝勵槸閻忔繂顕ｆ禒瀣厸闁割偆鍣ュ▓妯侯熆?
            moveCamera(CameraUpdateFactory.newLatLng(LatLng(Constant.LAT, Constant.LNG)))


            // 闂佽崵濮撮鍛村疮椤栫偛姹查柨婵嗩槸缁€鍐煕濞戝崬鐏ｉ柡瀣╃箖I padding 闂傚倸鍊搁崯顐㈩嚕閸洦鏁嗘繛宀€顥杇o闂佽崵鍋為崙褰掑磻婵犲啫顕遍柛鏇ㄥ灠缁?
            setPadding(0, 0, 0, SizeUtils.dp2px(220f))

            // 闂備胶鍘ч〃搴㈢濠婂牊鍋╅柣鎰靛墰閳绘洖顭跨捄渚Х缂佽妫濋弻鐔碱敇瑜嶉悘锟犳煟濡も偓瀹曨剟顢?
            setInfoWindowAdapter(HotelCustomInfoWindowAdapter(this@TravelMapActivity))

            // 闂備胶绮崝妤呭箠閹捐鍚规い鏇楀亾鐎殿喕绮欏畷鍫曨敂瀹ュ懏鏆?
            setOnMarkerClickListener(this@TravelMapActivity)

            // 闂備胶绮崝妤呭箠閹捐鍚规い鏇楀亾鐎殿喕绮欏畷鍫曨敂瀹ュ懏鏆┑鐑囩到濞茬娀宕滃┑鍥ь嚤闁告侗鍨抽悳缁樹繆閵堝倸浜鹃悷?
            setOnInfoWindowClickListener(this@TravelMapActivity)

            // 缂傚倷绀侀ˇ鎶筋敋瑜庨幈銊╁煛閸涱厾顦梺缁橆殔閻楀棝鎮樻径鎰厽闁靛鍎遍褏绱撳鍛櫧缂?
            setOnMyLocationButtonClickListener(this@TravelMapActivity)

            // 闂備胶绮崝妤呭箠閹捐鍚规い鏇楀亾妤犵偛顑夐獮瀣偐瀹曞洦娈稿┑鐐舵彧缁茬晫绮旈崼鏇熷仾?
            setOnMyLocationClickListener(this@TravelMapActivity)
            // 闂佸搫顦悧鍡涘箠鎼淬垺鍙忔い蹇撶墕缁€澶愭煟閺冨浂鍤欓柛妯虹摠娣囧﹪顢涘璇蹭壕鐎规洖娲ㄩ、鍛存⒑閹稿海鈽夐柣顓濈窔閵嗗懘顢曢敂钘夊壆闂佸搫绋侀崜锕€顭囬幒妤佺厱闁靛鍎辩紞鏍磼濡も偓閸婂潡骞嗛崘顔肩妞ゆ牗绮岀粭锟犳煟閻橀亶妾烽柛濠冩礋瀹曨剛鈧數纭堕崑鎾舵兜閸滀焦缍堥悗娈垮枟鐎笛冾焽?
            setContentDescription("Map with lots of markers.");
        }

        googleMap.uiSettings.apply {
            isMapToolbarEnabled = true
            // 闂傚倸鍊搁幊蹇涘礉濡綍娑樼暆閸曨偆顓洪梺鐟邦嚟婵敻鎮鹃崡鐏诲綊鏁愰崨顖呫倝鏌ｉ妶鍛棦鐎规洦浜炴禒锕傛嚍閵壯屾疮闂?
            // isMyLocationButtonEnabled = false
        }

        // 闁荤喐绮庢晶妤呭箰閸涘﹥娅犻柣妯挎珪婵鈧箍鍎卞ú銊╁几?
        enableMyLocation()

    }


    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (!::googleMap.isInitialized) return

        // 闂備線娼荤拹鐔煎礉鐏炴枻鑰块柟缁㈠枛閻愬﹪鏌涘▎宥呭姢闁荤喐鍔欓弻锟犲礋椤愨懇鎸冮梺闈涙处娣囨椽濡甸幇鏉跨闁告劕妯婇弸鈧┑鐐舵彧缁茬晫绮旈崼鏇熷仾闁糕剝绋掗弲顒傗偓鍏夊亾闁告洦鍓氶悾鑸电節閵忥絾纭炬い鏇ㄥ幒缁ㄦ椽姊洪崨濠冪厽闁告柨鐭傚畷鍝勭暆閸愶缚姹楅梺瑙勫劤閸熷潡路閸涱劶褰掓晲閸涱垍銈夋煟?
        PermissionUtils.permissionAny(
            this, {
                if (it) {
                    googleMap.isMyLocationEnabled = true
                    getMyLocationInfo()
                }
            }, Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        requestMakerList()
    }


    private fun requestMakerList() {
        if (!::googleMap.isInitialized) return
        presenter.getMakerData(currentRequestCategoryId, searchInput, requestLatitude, requestLongitude)
    }


    /**
     * 闂備礁鍚嬮崕鎶藉床閼艰翰浜归柛銉墮缁狅綁鏌熺€电校闁伙絽宕湁闁挎繂鎳愯倴闂佹眹鍊曞Λ娆戠矚闁秴鐒洪柛鎰屽懐顦?
     */
    private fun getMyLocationInfo() {
        LocationUtil.getInstance().addressCallback = object : LocationUtil.AddressCallback {
            override fun onGetAddress(address: Address) {
                val countryName = address.countryName //闂備焦鎮堕崕鏉懳涢崟顖ｆ晩?
                val adminArea = address.adminArea //闂?
                val locality = address.locality //闂?
                val subLocality = address.subLocality //闂?
                val featureName = address.featureName //闂佽崵鍋炵粙鎺椼€冩繝鍐洸?
            }

            override fun onGetLocation(lat: Double, lng: Double) {
                CacheUtil.getShareDefault().put(Constant.SP_MY_LATITUDE, lat.toFloat())
                CacheUtil.getShareDefault().put(Constant.SP_MY_LONGITUDE, lng.toFloat())

                // 闂傚倸鍊甸崑鎾寸節婵犲倸鏆斿ù鐘茬墢缁辨帗寰勬惔顔兼婵犳鍠掗弲鐘茬暦濮樿泛绾ч悹鎭掑妿姝囬梻浣告啞閹稿摜绮斿畷鍥╃闁告侗鍨崇壕?
                myLocation = CameraPosition.Builder().target(LatLng(lat, lng))
                    .zoom(20f)
                    .bearing(0f)
                    .tilt(0f)
                    .build()
                animateCamera(LatLng(lat, lng))

            }
        }
    }


    override fun bindMakerList(response: MutableList<TravelChild>) {
        if (!::googleMap.isInitialized) return

        if (response.isEmpty()) {
            ToastUtils.showShort(R.string.search_result_empty_hint)
            googleMap.clear()
            makers.clear()
            return
        }

        // 闂備胶鍎甸弲娑㈠箟閳ユ枼鏀﹂柍褜鍓涚槐鎺撳緞鎼搭喖娈繝娈垮枓閺呯姴鐣峰鑸靛剬缂佺儤绗抏r闂備浇鍋愰崢褔宕鈷?
        val bounds = LatLngBounds.Builder()

        response.forEach {
            bounds.include(LatLng(it.lat, it.lng))
        }

//        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 300))


        // 闂備線娼荤拹鐔煎礉鐏炴枻鑰块柟缁㈠枛閻愬﹪鏌涘▎宥呭姢闁荤喐鍔栫换娑㈡晲閸偄娈繝娈垮枤閸嬬偛顕ラ崟顕呮▌闂侀潻绲块惁鐬恟ker
        addMarkersToMap(response)

        // 闂佽绻掗崑鐐裁哄鍫澪︽慨婵堟晿rker 闂備礁鎲＄敮妤呫€冩径鎰ラ柛鎰靛枛閺嬩線鏌ｅΔ鈧悧鍡欑矈?
        mMarkerContentAdapter.setNewInstance(response)

        if (needMoveCamera) {
            animateCamera(LatLng(requestLatitude, requestLongitude))
            needMoveCamera = false
        }
    }

    /**
     * 闂備礁鎲＄喊宥夊垂闂堟冻鑰块柟缁㈠枛閻愬﹪鏌涘▎宥呭姤闁告﹩鍓氱换娑㈡晲閸偄娈繝?Maker
     */
    private fun addMarkersToMap(response: MutableList<TravelChild>) {
        if (!::googleMap.isInitialized || response.isEmpty()) return
        googleMap.clear()
        makers.clear()

        response.forEach {

            val view = LayoutInflater.from(this).inflate(R.layout.custom_info_contents_2, null)
            val title_tv = view.findViewById<TextView>(R.id.title_tv)
            val image_iv = view.findViewById<ImageView>(R.id.image_iv)
            title_tv.text = it.name
            image_iv.setImageResource(R.mipmap.ic_marker_common_travel)

            val maker = googleMap.addMarker(
                MarkerOptions()
                    .position(LatLng(it.lat, it.lng))
                    .title(it.name)
                    .snippet("")
                    .icon(BitmapDescriptorFactory.fromBitmap(ConvertUtils.view2Bitmap(view)))
            )
            maker?.tag = it
            makers.add(maker)
        }

    }


    override fun onMarkerClick(marker: Marker): Boolean {

        // 闂備礁婀遍。浠嬪磻閹剧粯鐓?maker 闂佽崵濮崇粈浣规櫠娴犲鍋柛鈩冪☉缁狅綁鏌熼柇锕€骞楃€殿喗鎹囧娲敃閵忊晜啸闂?
        makers.forEach {
            val makerTag = it?.tag as TravelChild
            val view = LayoutInflater.from(this).inflate(R.layout.custom_info_contents_2, null)
            val title_tv = view.findViewById<TextView>(R.id.title_tv)
            title_tv.text = makerTag.name
            val image_iv = view.findViewById<ImageView>(R.id.image_iv)
            image_iv.setImageResource(R.mipmap.ic_marker_common_travel)

            it.setIcon(BitmapDescriptorFactory.fromBitmap(ConvertUtils.view2Bitmap(view)))

        }
        marker.hideInfoWindow()

        // 闂佽崵濮崇粈浣规櫠娴犲鍋柛鈩冨搸娴滃綊鏌熼幆褍鏆辨い銈呮嚇濮婃椽顢曢姀鈺傂ラ梺?
        val currentMakerTag = marker.tag as TravelChild
        val view = LayoutInflater.from(this).inflate(R.layout.custom_info_contents_2, null)
        val title_tv = view.findViewById<TextView>(R.id.title_tv)
        title_tv.text = currentMakerTag.name
        val image_iv = view.findViewById<ImageView>(R.id.image_iv)
        image_iv.setImageResource(R.mipmap.ic_marker_select_travel)
        marker.setIcon(BitmapDescriptorFactory.fromBitmap(ConvertUtils.view2Bitmap(view)))

        // 闂備礁鎲＄敮鎺懨洪敃鈧悾鐑藉矗婢跺鍔呴梺鍝勬储閸ㄥ綊宕濋妷鈺傜厱闁圭儤鎼╁▓娆撴煏閸℃鏆ｇ€殿噮鍋婇幃褔宕煎┑鍫涘亰
        val index = makers.indexOf(marker)
        if (index != -1) {
            binding.bottomSheetRv.scrollToPosition(index)
            makerBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        // 闂備線娼婚梽鍕熆濡ソ鐟邦潨閳ь剟鎮伴鈧幖褰掝敃閵堝棗顏ょ紓鍌欑椤︽娊顢氳閹便劑鍩￠崨顓狀槴闂佽顔栭崰妤冪玻濡ゅ懏鐓涚€广儱鎳忛惌妤冪磽瀹ュ懏鍣界紒?
        animateCamera(marker.position)
        return false
    }

    private fun animateCamera(location: LatLng) {
        Handler(mainLooper).postDelayed({
            val targetCamera = CameraPosition.Builder().target(location)
                .zoom(13f)
                .bearing(0f)
                .tilt(0f)
                .build()
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(targetCamera))

        }, 10)
    }

    override fun onMyLocationButtonClick(): Boolean {
        if (!::myLocation.isInitialized) return false
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(myLocation))
        return false
    }

    override fun onMyLocationClick(location: Location) {
//        ToastUtils.showShort("Current location : \nlatitude:${location.latitude}" + "\nlongitude:${location.longitude}  ")
    }


    override fun onInfoWindowClick(marker: Marker) = Unit


    // ####################  缂傚倷鐒︾粙鎺楁儎椤栫偛鐒?###############


    override fun bindContentList(response: MutableList<TravelChild>, lastPage: Boolean) {
        binding.swipeLayout.isRefreshing = false
        val loadMoreModule = mAdapter.loadMoreModule
        if (response.isEmpty()) {
            if (page == Constant.DEFAULT_FIRST_PAGE) {
                mAdapter.setNewInstance(null)
                loadMoreModule.loadMoreComplete()
                emptyView.showEmptyContainer()
            } else {
                loadMoreModule.loadMoreEnd()
            }
            return
        }
        if (page == Constant.DEFAULT_FIRST_PAGE) {
            mAdapter.setNewInstance(response)
            loadMoreModule.checkDisableLoadMoreIfNotFullPage()

        } else {
            mAdapter.addData(response)
            if (lastPage) {
                loadMoreModule.loadMoreEnd()
            } else {
                loadMoreModule.loadMoreComplete()
            }
        }
    }

    override fun bindCityList(cityList: MutableList<CityPickerResponse.CityChild>) {
        cityFilterAdapter.setNewInstance(cityList)
    }


}

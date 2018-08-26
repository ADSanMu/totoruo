

JsonObject jsonObj = ApiOperator 
						.get("/ssmp/endeca/search")//返回一个RequestEntityBuilder
						.dynamicVersion()
						.param("keyword","australia")
						.cookie("cookieName","cookieValue")
						.header("headerName","headerValue")	
						.request()//返回一个responseEntityHolder()
						.ifNotOk(Object)//如果不是200、或者有ApiException出现（就不会抛出异常了）就使用该 object 作为alternative请求相应的内容
						.addConsumers(new Consumer(){})
						.toJsonObj();

创建一个ApiResponseHolder,下面有几个子类：ErrorResponseHolder(请求中发生了异常) OkResponseHolder(200),DefaultResponseHolde(非200)

ApiResponseHolder是一个abstract类，有方法 newApiResponseHolder(CloseableClientHttpResponse)根据状态码来决定返回那个子类的实例。需要关闭CloseableClientHttpResponse
如果请求中抛出了异常，就直接返回 ErrorResponseHolder类的实例。

ApiResponseHolder封装了返回的CloseableClientHttpResponse中各种属性。他的子类里面提供了业务逻辑方法

在ApiContext中有方法： ApiResponseHolder request(){
	
			ApiResponseHolder.newApiResponseHolder(this.requestEntity);//在方法newApiResponseHolder里面调用请求
			
}
在ApiResponseHolder里面会维护一个RequestEntity的引用


//在apiServlet中需要
ApiOperator.dynamic().dynamicVersion().all().request().isOk("Request remote resources failed with ${apiPath}").response();//"在请求参数中必须要有一个叫做apiPath的参数"

//模拟请求后台的blogs
JsonObject jsonObj = ApiOperator.get("/blog/blogs").request().toJsonObj();//如果返回的结果不是json格式或者说不是jsonObject，抛出ApiException.

//模拟blog新增event触发时添加blogid的请求
ApiOperator
.post("/blog/post")
.param("blogId","12345677")
.request()
.isOK("Create a new post's id failed, Please contact admin to sovle this issue!");//isOK()方法断言请求一定返回200 如果你不需要response返回的内容 ，请务必使用isOk()方法

//模拟用户登录的请求
ApiOperator.post("/blog/login").param("username","mockname").param("password","xxxx").request().response();

//staitstic 统计使用ApiOperator发出请求中，不是200的请求，并定时的写回到AEM节点中，方便查看。

// 创建一个exception: ApiException extends RuntimeException 当在请求准备、中、返回结果处理：发生的一切异常都会抛出该异常类。


//在上述的fluent方法都不会抛出受检异常，也就是说说当出现请求失败（比如 host error）会抛出 ApiException（运行时异常）


public final class ApiOperator{

	RequestEntityBuilder get(String apiPath){
		return new RequestEntityBuilder(new HttpGet(apiPath));
	}

	RequestEntityBuilder post(String apiPath){
		return new RequestEntityBuilder(new HttpPost(apiPath));
	}

	RequestEntityBuilder dynamic(){
		//get current method 
	}

	RequestStat stat();

	class RequestStat{

		class RequestRecord{

		}
	}

}



public class RequestEntityBuilder{

	private RequestEntity requestEnity;

	RequestEntityBuilder(HttpRequestBase httpBaseRequest)｛
		this.requestEnity = new RequestEntity(httpBaseRequest);
	｝

	public RequestEntityBuilder dynamicVersion();

	public RequestEntityBuilder param(String name,Object value);//value 是直接toString();

	public RequestEntityBuilder header(String name,String... value);

	public RequestEntityBuilder cookie(String name,String value);

	public RequestEntityBuilder all();

	public RequestEntityBuilder consumer(Consumer<ApiResponseHolder>... consumers);

	public RequestEntityBuilder ifNotOk(Object object);

	public ApiResonseProcessor request(){
    	return ApiResponseHolderBuilder.newApiResponseHolder(this.requestEnity.completeApiPath());
	}

}

class ApiResponseHolderBuilder{

	private static ApiResonseProcessor newApiResponseHolder(RequestEntity requestEnity){
		ApiResonseProcessor apiResponseHolder;
		try(CloseableClientHttpResponse response = requestEnity.request();){
			apiResponseHolder = this.extract(response);
		}catch(Throwable t){
			apiResponseHolder = new ErrorApiResponseHolder(t);
		}
		return apiResponseHolder;
	}

	private static ApiResponseHolder extract(CloseableClientHttpResponse response){
		//获取response status来决定返回的是OkApiResponseHolder/DefaultResponseHolde 并且把相应的内容填充到实体类中
	}

}

class RequestEntity{

	private List<ParamPair> params;

	private HttpBaseRequest httpBaseRequest;

	private List<Consumer<ApiResponseHolder>> consumers;

	private	Object alternative;

	RequestEntity(HttpBaseRequest httpBaseRequest);

	RequestEntity completeApiPath();

	CloseableClientHttpResponse	request(){}

}


public interface ApiResponseProcessor{

	ApiResonseProcessor isOk(String errorMsg);
	
	ApiResonseProcessor noError(String errorMsg);

	JsonObject toJsonObj();

	JsonArray toJsonArray();

	<T>T toBean(Class<T> beanType);

	void response();

}



abstract class ApiResponseHolder implements ApiResponseProcessor{

	RequestEntity requestEnity;

	int status;

	MediaType mediaType;

	Object response;

	MultiValueMap headers;

	List<Cookie> cookies;

	ApiResponseHolder(){
		(Comsumer<ApiResponseHolder> consumer:this.requestEnity.getConsumers()){
			consumer.accept(this);
		}
	}

	String parseMsg(String msg)｛

	｝//解析isOk("${apiPath}")等特定的字符


}

class OkApiResponseHolder extends ApiResponseHolder{


}

class DefaultApiResponseHolder extends ApiResponseHolder{


}


class ErrorApiResponseHolder extends ApiResponseHolder{

	//如果没有设置ifNotOk(),全部抛出ApiException

}

public class ApiException extends RuntimeException{

}


ApiOperator.get("/ssmp/endeca/search").dynamicVersion().param("keyword","australia").request().toJsonObj();
ApiOperator.dynamic().dynamicVersion().all().request().isOk("Request api failed with apiPath = '${apiPath}' please check").response();

上面的都只以接口的形式暴露出来，不把实际的实现类暴露出来，用户只需要知道ApiOperator这个类够了。
大概需要的接口有：RequestEntityBuilder 和 ApiResonseProcessor，然后把默认的实现类都隐藏起来。


############################################################################################
* RequestEntityBuilder 里面增加方法：getParams()

* ApiContext 维护一个RequestBuilder(Apache) 对象 修改RequestEntity中的重复的地方，使用apache已实现的

* RequestEntity 里面只有apiContext, 方法包含了执行当前请求， 且包含了提供当前请求的所有信息

* consumer改成一个参数是apiContext，现在分成了两个

* 实现一个CookieStore 继承BasicCookieStore，去除掉过期的cookie和上次请求携带的cookie(没有改变的) 重写getCookies方法

* 清除掉在远程服务器返回的header中不需要的header(Set-Cookie(cookies放在cookieStore里面来写回到页面),Content-Length,Date,Content-Type(该属性去掉由aem response), wso2's headers)

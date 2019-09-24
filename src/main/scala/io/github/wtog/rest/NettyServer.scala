package io.github.wtog.rest

import java.net.URI

import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.Unpooled
import io.netty.channel._
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http._
import io.netty.handler.logging.{ LogLevel, LoggingHandler }
import io.netty.handler.ssl.SslContext

/**
  * @author : tong.wang
  * @since : 2019-08-28 10:24
  * @version : 1.0.0
  */
object NettyServer extends Server {
  override def doStart(routes: Set[Router]) = {
    val bossGroup   = new NioEventLoopGroup(1)
    val workerGroup = new NioEventLoopGroup()
    try {
      val bootstrap = new ServerBootstrap()
      bootstrap
        .group(bossGroup, workerGroup)
        .channel(classOf[NioServerSocketChannel])
        .handler(new LoggingHandler(LogLevel.INFO))
        .childHandler(new ServerInitializer(routes = routes ++ defaultRoutes))

      val channel = bootstrap.bind(port).sync().channel()
      channel.closeFuture().sync()
    } finally {
      bossGroup.shutdownGracefully()
      workerGroup.shutdownGracefully()
    }
  }

}

class ServerInitializer(routes: Set[Router], sslContext: Option[SslContext] = None) extends ChannelInitializer[SocketChannel] {

  override def channelReadComplete(ctx: ChannelHandlerContext): Unit = ctx.flush()

  override def initChannel(channel: SocketChannel) =
    channel
      .pipeline()
      .addLast(new HttpRequestDecoder)
      .addLast(new HttpResponseEncoder)
      .addLast(new HttpObjectAggregator(1024))
      .addLast(new RouterHandler(routes))

}

class RouterHandler(routes: Set[Router]) extends SimpleChannelInboundHandler[FullHttpRequest](true) {

  import io.netty.handler.codec.http.HttpResponseStatus._

  def channelRead0(ctx: ChannelHandlerContext, request: FullHttpRequest): Unit =
    try {
      val uri    = new URI(request.uri()).getPath
      val method = request.method().name()

      routes.find(route => route.method == method && route.route == uri) match {
        case Some(handler) =>
          responseOk(request, handler.handleRequest(request))(ctx)
        case None =>
          responseNotFound(request, "not found".getBytes())(ctx)
      }
    } catch {
      case e: Throwable =>
        responseBadRequest(request, e.getLocalizedMessage.getBytes())(ctx)
    }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    cause.printStackTrace()
    ctx.close()
  }

  private[this] def responseBadRequest(request: FullHttpRequest, resp: Array[Byte])(ctx: ChannelHandlerContext) =
    response(ctx, BAD_REQUEST, request, resp)

  private[this] def responseOk(request: FullHttpRequest, resp: Array[Byte])(ctx: ChannelHandlerContext) =
    response(ctx, OK, request, resp)

  private[this] def responseNotFound(request: FullHttpRequest, resp: Array[Byte])(ctx: ChannelHandlerContext) =
    response(ctx, NOT_FOUND, request, resp)

  private[this] def response(ctx: ChannelHandlerContext, status: HttpResponseStatus, req: FullHttpRequest, resp: Array[Byte]) = {
    import io.netty.handler.codec.http.HttpHeaderNames._
    import io.netty.handler.codec.http.HttpVersion._

    val keepAlive = HttpUtil.isKeepAlive(req)
    val content   = Unpooled.copiedBuffer(resp)
    val response  = new DefaultFullHttpResponse(HTTP_1_1, status, content)

    response.headers.set(HttpHeaderNames.CONTENT_TYPE, "text/plain")
    response.headers.set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes)

    if (!keepAlive) {
      ctx.write(response).addListener(ChannelFutureListener.CLOSE)
    } else {
      response.headers().set(CONNECTION, HttpHeaderNames.KEEP_ALIVE);
      ctx.write(response)
    }
    ctx.flush()
  }

}

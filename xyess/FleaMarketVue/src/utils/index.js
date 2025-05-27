import { useGlobalStore } from '@/stores'

/**
 * 处理图片URL，确保正确的路径前缀
 * @param {string} url 原始URL
 * @returns {string} 处理后的完整URL
 */
export const getImageUrl = (url) => {
  const store = useGlobalStore()
  
  // 如果URL为空，返回占位图
  if (!url) {
    return '/placeholder.jpg'
  }
  
  try {
    // 如果是数组字符串，解析并获取第一个元素
    if (typeof url === 'string' && url.startsWith('[')) {
      try {
        const urlArray = JSON.parse(url)
        if (Array.isArray(urlArray) && urlArray.length > 0) {
          url = urlArray[0]
        }
      } catch (e) {
        // 解析URL数组失败
      }
    }
    
    // 确保url是字符串
    let imageUrl = String(url).trim().replace(/^["']|["']$/g, '')
    
    // 如果已经是完整URL，直接返回
    if (imageUrl.startsWith('http')) {
      return imageUrl
    }
    
    // 构建完整的图片URL
    const baseApi = store.baseApi
    
    // 如果URL不包含file前缀，添加它
    if (!imageUrl.includes('/file/')) {
      if (imageUrl.startsWith('/image/')) {
        imageUrl = imageUrl.replace('/image/', '/file/image/')
      } else if (!imageUrl.startsWith('/')) {
        imageUrl = `/file/image/${imageUrl}`
      } else {
        imageUrl = `/file${imageUrl}`
      }
    }
    
    const fullUrl = `${baseApi}${imageUrl.startsWith('/') ? imageUrl : '/' + imageUrl}`
    return fullUrl
  } catch (e) {
    // 处理图片URL出错
    return '/placeholder.jpg'
  }
}

/**
 * 处理商品图片列表
 * @param {string} pictureList 图片列表字符串
 * @returns {Array} 处理后的图片URL数组
 */
export const getImageUrls = (pictureList) => {
  if (!pictureList) {
    return []
  }
  
  try {
    // 解析图片列表
    let urls
    if (typeof pictureList === 'string') {
      try {
        urls = JSON.parse(pictureList)
      } catch (e) {
        // 如果解析失败，可能是单个URL
        return [getImageUrl(pictureList)]
      }
    } else {
      urls = pictureList
    }
    
    // 确保urls是数组
    if (Array.isArray(urls)) {
      return urls.map(url => getImageUrl(url))
    } else {
      return [getImageUrl(urls)]
    }
  } catch (e) {
    // 处理图片列表出错
    return []
  }
} 
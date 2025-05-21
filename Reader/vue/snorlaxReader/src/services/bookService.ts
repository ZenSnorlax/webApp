import request from "@/utils/request.ts";

export interface BookSearchInfo {
    pageNum: number,
    pageSize: number,
    keywords: string
    total: number,
    bookInfos: BookInfo[]
}

export async function searchBook(keywords: string, pageNum: number, pageSize: number) {

    try {
        const response = await request.get("/book/search", { params: { keywords, pageNum, pageSize } });
        if (response.status !== 200) {
            console.error("搜索书籍失败:", response.statusText);
        }
        return response.data;
    } catch (error) {
        console.error("搜索书籍失败:", error);
        throw error;
    }
}
// 回显书籍信息
export interface BookInfo {
    id: number;
    coverUrl: string;
    title: string;
    author: string;
    publisher: string;
    publishDate: Date | null;
    description: string;
    language: string;
}

export async function uploadBook(file: File) {
    try {
        const formData = new FormData();
        formData.append("file", file);

        const response = await request.post("/book/upload", formData, {
            headers: {
                "Content-Type": "multipart/form-data"
            },
            timeout: 1000000000
        });

        if (response.status !== 200) {
            alert("上传书籍失败:" + response.statusText);
            console.error("上传书籍失败:", response.statusText);
        }
        return response.data;
    } catch (error) {
        console.error("上传书籍失败:", error);
        throw error;
    }
}

//已上传书籍信息
export interface UpListBookInfo {
    pageNum: number,
    pageSize: number,
    total: number,
    bookInfos: BookInfo[]
}

export async function getUpListBook(pageNum: number, pageSize: number) {
    try {
        const response = await request.get("/book/upload/list", { params: { pageNum, pageSize } });
        if (response.status !== 200) {
            console.error("获取已上传书籍失败:", response.statusText);
        }
        return response.data;
    } catch (error) {
        console.error("获取已上传书籍失败:", error);
        throw error;
    }
}

// 获取图书临时链接
export async function generateBookTempUrl(id: number) {
    try {
        const response = await request.get(`/book/${id}`);
        if( response.status !== 200){
             console.error("获取图书临时链接失败:", response.statusText);
        }
        return response.data;
    } catch (error) {
        console.error("获取图书临时链接失败:", error);
        throw error;
    }
}
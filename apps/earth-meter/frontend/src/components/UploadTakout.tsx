import {MouseEventHandler, useCallback, useMemo, useState} from "react";
import {UploadApi, Configuration, ResponseError} from "earth-meter-api"
import {useDropzone} from "react-dropzone";
import {User} from "oidc-client-ts";
import {Accordion, Button} from "react-bootstrap";
import {BallTriangle} from 'react-loader-spinner'
import "./UploadTakout.scss";

const apiConfig = new Configuration({
    basePath: '/api/earth-meter/v1'
});
//
const uploadApi = new UploadApi(apiConfig);

interface Props {
    user: User;
}

export const UploadTakeout = (props: Props) => {
    const {user} = props;

    const [uploadProgressing, setUploadProgressing] = useState(false);

    const {acceptedFiles, getRootProps, getInputProps} = useDropzone({
        maxFiles: 2,
        accept: {
            'application/zip': ['.zip']
        }
    });

    const files = useMemo(() => acceptedFiles.map(file => (
        <li key={file.path}>
            {file.path} - {file.size} bytes
        </li>
    )), [acceptedFiles]);


    const handleUpload: MouseEventHandler<HTMLButtonElement> = useCallback(e => {
        setUploadProgressing(true)
        uploadApi.uploadTakeout({
            body: acceptedFiles[0]
        }, {
            headers: {
                "Content-Type": "application/zip",
                "Authorization": "Bearer " + user.access_token
            }
        })
            .finally(() => setUploadProgressing(false))
            .catch(errorAlert)
        e.stopPropagation();
        return false;
    }, [acceptedFiles, setUploadProgressing, user])

    return (
        <section className="upload-takeout">
            <div {...getRootProps({className: 'dropzone'})}>
                <input {...getInputProps()} />
                {acceptedFiles.length == 0 ?
                    <>Drag 'n' drop some files here, or click to select files</>
                    : <>
                        <ul>{files}</ul>
                        {uploadProgressing ? <BallTriangle height={40}/> :
                            <Button disabled={uploadProgressing} onClick={handleUpload}
                                    size="sm">Upload</Button>}
                    </>
                }
            </div>
        </section>
    );
};
